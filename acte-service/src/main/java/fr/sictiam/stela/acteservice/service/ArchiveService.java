package fr.sictiam.stela.acteservice.service;

import fr.sictiam.stela.acteservice.service.exceptions.ActeNotFoundException;
import fr.sictiam.stela.acteservice.dao.ActeRepository;
import fr.sictiam.stela.acteservice.dao.AdminRepository;
import fr.sictiam.stela.acteservice.dao.EnveloppeCounterRepository;
import fr.sictiam.stela.acteservice.model.*;
import fr.sictiam.stela.acteservice.model.event.ActeHistoryEvent;
import fr.sictiam.stela.acteservice.model.xml.*;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import javax.xml.bind.JAXBElement;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class ArchiveService implements ApplicationListener<ActeHistoryEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveService.class);

    @Value("${application.archive.maxSize}")
    private Integer archiveMaxSize;

    private String departement = "006";
    private String arrondissement = "2";
    private String siren = "210600730";
    private String trigraph = "SIC";

    private final ActeRepository acteRepository;
    private final Jaxb2Marshaller jaxb2Marshaller;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final EnveloppeCounterRepository enveloppeCounterRepository;
    private final AdminRepository adminRepository;

    public ArchiveService(ActeRepository acteRepository, Jaxb2Marshaller jaxb2Marshaller, ApplicationEventPublisher applicationEventPublisher,
                          EnveloppeCounterRepository enveloppeCounterRepository, AdminRepository adminModuleRepository) {
        this.acteRepository = acteRepository;
        this.jaxb2Marshaller = jaxb2Marshaller;
        this.applicationEventPublisher = applicationEventPublisher;
        this.enveloppeCounterRepository = enveloppeCounterRepository;
        this.adminRepository= adminModuleRepository;
    }

    /**
     * Compress file and annexes into a tar.gz archive.
     */
    private void createArchive(String acteUuid) {
        Acte acte = acteRepository.findByUuidAndDraftFalse(acteUuid).orElseThrow(ActeNotFoundException::new);

        try {
            int deliveryNumber = getNextIncrement();

            // this is the base filename for the message and attachments
            String baseFilename = getBaseFilename(acte, Flux.TRANSMISSION_ACTE);

            String acteFilename =
                    String.format("CO_DE-%s_%d.%s", baseFilename, 1, StringUtils.getFilenameExtension(acte.getActeAttachment().getFilename()));

            Map<String, byte[]> annexes = new HashMap<>();
            acte.getAnnexes().forEach(attachment -> {
                // sequence 1 is taken by the Acte file, so we start at two
                int sequence = annexes.size() + 2;
                String tempFilename =
                        String.format("CO_DE-%s_%d.%s", baseFilename, sequence,
                                StringUtils.getFilenameExtension(attachment.getFilename()));
                annexes.put(tempFilename, attachment.getFile());
            });

            String messageFilename = String.format("%s_%d.xml", baseFilename, 0);
            JAXBElement<DonneesActe> donneesActe = generateDonneesActe(acte, acteFilename, annexes.keySet());
            String messageContent = marshalToString(donneesActe);

            String enveloppeName = String.format("EACT--%s--%s-%d.xml", siren, getFormattedDate(LocalDate.now()), deliveryNumber);
            JAXBElement<DonneesEnveloppeCLMISILL> donneesEnveloppeCLMISILL1 = generateEnveloppe(acte, messageFilename);
            String enveloppeContent = marshalToString(donneesEnveloppeCLMISILL1);

            String archiveName = getArchiveName(enveloppeName);

            ByteArrayOutputStream baos =
                    createArchiveAndCompress(enveloppeName, enveloppeContent, messageFilename, messageContent,
                            acte.getActeAttachment().getFile(), acteFilename, annexes);

            ActeHistory acteHistory = new ActeHistory(acte.getUuid(), StatusType.ARCHIVE_CREATED, LocalDateTime.now(),
                    baos.toByteArray(), archiveName);

            applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory));

            LOGGER.info("Archive created : {}", archiveName);
        } catch (Exception e) {
            LOGGER.error("Error while generating archive for acte {} : {}", acte.getNumber(), e.getMessage());
            ActeHistory acteHistory = new ActeHistory(acte.getUuid(), StatusType.FILE_ERROR, LocalDateTime.now(),
                    e.getMessage());
            applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory));
        }
    }

    private void createCancellationMessage(String acteUuid) {

        Acte acte = acteRepository.findByUuidAndDraftFalse(acteUuid).orElseThrow(ActeNotFoundException::new);

        try {
            LOGGER.debug("Creating cancellation message for acte {}", acteUuid);

            int deliveryNumber = getNextIncrement();

            String baseFilename = getBaseFilename(acte, Flux.ANNULATION_TRANSMISSION);
            String enveloppeName = String.format("EACT--%s--%s-%d.xml", siren, getFormattedDate(LocalDate.now()), deliveryNumber);
            String messageFilename = String.format("%s_%d.xml", baseFilename, 0);

            ObjectFactory objectFactory = new ObjectFactory();
            Annulation annulation = objectFactory.createAnnulation();
            String idActe = String.format("%s-%s-%s-%s-%s",
                    departement,
                    siren,
                    acte.getDecision().format(DateTimeFormatter.ofPattern("YYYYMMdd")),
                    acte.getNumber(),
                    acte.getNature().getAbbreviation());
            annulation.setIDActe(idActe);

            StringWriter sw = new StringWriter();
            jaxb2Marshaller.marshal(annulation, new StreamResult(sw));

            String archiveName = getArchiveName(enveloppeName);

            JAXBElement<DonneesEnveloppeCLMISILL> donneesEnveloppeCLMISILL1 = generateEnveloppe(acte, messageFilename);
            String enveloppeContent = marshalToString(donneesEnveloppeCLMISILL1);

            ByteArrayOutputStream baos =
                    createArchiveAndCompress(enveloppeName, enveloppeContent, messageFilename, sw.toString(),
                            null, null, null);

            ActeHistory acteHistory = new ActeHistory(acte.getUuid(), StatusType.CANCELLATION_ARCHIVE_CREATED,
                    LocalDateTime.now(), baos.toByteArray(), archiveName);

            applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory));

            LOGGER.info("Cancellation archive created : {}", archiveName);
        } catch (IOException e) {
            LOGGER.error("Error while generating archive for cancellation of acte {} : {}", acte.getNumber(), e.getMessage());
            ActeHistory acteHistory = new ActeHistory(acte.getUuid(), StatusType.FILE_ERROR, LocalDateTime.now(), e.getMessage());
            applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory));
        }
    }

    private void checkArchiveSize(ActeHistory acteHistory) {
        LOGGER.debug("Archive size is {} (max allowed : {})", acteHistory.getFile().length, archiveMaxSize);
        if (acteHistory.getFile().length > archiveMaxSize) {
            // TODO need a specific message or is it enough with the status type ?
            ActeHistory newActeHistory = new ActeHistory(acteHistory.getActeUuid(), StatusType.ARCHIVE_TOO_LARGE);
            applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, newActeHistory));
        } else {
            ActeHistory newActeHistory = new ActeHistory(acteHistory.getActeUuid(), StatusType.ARCHIVE_SIZE_CHECKED);
            applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, newActeHistory));
        }
    }

    private String getArchiveName(String enveloppeName) {
        return String.format("%s-%s.%s",
                trigraph,
                StringUtils.stripFilenameExtension(enveloppeName),
                "tar.gz");
    }

    private String getBaseFilename(Acte acte, Flux flux) {
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYYMMdd"));

        return String.format("%s-%s-%s-%s-%s-%s-%s",
                departement,
                siren,
                today,
                acte.getNumber(),
                acte.getNature().getAbbreviation(),
                flux.getTransactionNumber(),
                flux.getFluxNumber());
    }

    private JAXBElement<DonneesEnveloppeCLMISILL> generateEnveloppe(Acte acte, String messageFilename) {

        IDCL idcl = new IDCL();
        idcl.setDepartement(departement);
        idcl.setArrondissement(arrondissement);
        idcl.setNature(acte.getNature().getCode());
        idcl.setSIREN(siren);
        
        Admin adminModule=adminRepository.findAll().get(0);
        Referent referent = new Referent();
        // TODO extract to local authority config
        referent.setEmail(adminModule.getMainEmail());
        referent.setNom("Demat SICTIAM");
        referent.setTelephone("0101010101");

        DonneesEnveloppeCLMISILL.Emetteur emetteur = new DonneesEnveloppeCLMISILL.Emetteur();
        emetteur.setIDCL(idcl);
        emetteur.setReferent(referent);

        DonneesEnveloppeCLMISILL.AdressesRetour adressesRetour = new DonneesEnveloppeCLMISILL.AdressesRetour();
        adressesRetour.getEmail().add(adminModule.getMainEmail());
        for (String email : adminModule.getAdditionalEmails()) {
        	 adressesRetour.getEmail().add(email);
		}
       

        // TODO is it really the message file that is expected here ?
        FichierSigne fichierSigne = new FichierSigne();
        fichierSigne.setNomFichier(messageFilename);
        FormulairesEnvoyes formulairesEnvoyes = new FormulairesEnvoyes();
        formulairesEnvoyes.getFormulaire().add(fichierSigne);

        DonneesEnveloppeCLMISILL donneesEnveloppeCLMISILL = new DonneesEnveloppeCLMISILL();
        donneesEnveloppeCLMISILL.setEmetteur(emetteur);
        donneesEnveloppeCLMISILL.setAdressesRetour(adressesRetour);
        donneesEnveloppeCLMISILL.setFormulairesEnvoyes(formulairesEnvoyes);

        ObjectFactory objectFactory = new ObjectFactory();
        return objectFactory.createEnveloppeCLMISILL(donneesEnveloppeCLMISILL);
    }

    private JAXBElement<DonneesActe> generateDonneesActe(Acte acte, String acteFilename, Set<String> annexesFilenames) {

        ObjectFactory objectFactory = new ObjectFactory();

        DonneesActe donneesActe = new DonneesActe();
        // currently just hardcoding the first two as they are the only used
        String[] codes = acte.getCode().split("-");
        DonneesActe.CodeMatiere1 codeMatiere1 = new DonneesActe.CodeMatiere1();
        codeMatiere1.setCodeMatiere(Integer.valueOf(codes[0]));
        donneesActe.setCodeMatiere1(codeMatiere1);
        DonneesActe.CodeMatiere2 codeMatiere2 = new DonneesActe.CodeMatiere2();
        codeMatiere2.setCodeMatiere(Integer.valueOf(codes[1]));
        donneesActe.setCodeMatiere2(codeMatiere2);
        donneesActe.setCodeNatureActe(Integer.valueOf(acte.getNature().getCode()));
        donneesActe.setDate(acte.getDecision());
        donneesActe.setNumeroInterne(acte.getNumber());
        donneesActe.setClassificationDateVersion(acte.getLocalAuthority().getNomenclatureDate());
        donneesActe.setObjet(acte.getObjet());

        FichierSigne fichierSigne = new FichierSigne();
        fichierSigne.setNomFichier(acteFilename);
        donneesActe.setDocument(fichierSigne);

        DonneesActe.Annexes annexes = new DonneesActe.Annexes();
        annexes.setNombre(String.valueOf(annexesFilenames.size()));
        annexesFilenames.forEach(annexeFilename -> {
            FichierSigne fichierSigne1 = new FichierSigne();
            fichierSigne1.setNomFichier(annexeFilename);
            annexes.getAnnexe().add(fichierSigne1);
        });
        donneesActe.setAnnexes(annexes);

        return objectFactory.createActe(donneesActe);
    }

    private String marshalToString(JAXBElement<?> object) {
        StringWriter sw = new StringWriter();
        jaxb2Marshaller.marshal(object, new StreamResult(sw));
        return sw.toString();
    }

    private ByteArrayOutputStream createArchiveAndCompress(String enveloppeName, String enveloppeContent,
                                                           String messageFilename, String messageContent,
                                                           byte[] acteFile, String acteFilename,
                                                           Map<String, byte[]> annexes) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);

        addEntry(enveloppeName, enveloppeContent.getBytes(), taos);
        addEntry(messageFilename, messageContent.getBytes(), taos);
        if (acteFile != null)
            addEntry(acteFilename, acteFile, taos);
        if (annexes != null) {
            for (String annexeName : annexes.keySet()) {
                addEntry(annexeName, annexes.get(annexeName), taos);
            }
        }

        taos.close();
        baos.close();

        return compress(baos);
    }

    private void addEntry(String entryName, byte[] content, TarArchiveOutputStream taos) throws IOException {
        File file = new File(entryName);
        FileCopyUtils.copy(content, file);
        ArchiveEntry archiveEntry = new TarArchiveEntry(file, entryName);
        taos.putArchiveEntry(archiveEntry);
        IOUtils.copy(new FileInputStream(file), taos);
        taos.closeArchiveEntry();
        file.delete();
    }

    private ByteArrayOutputStream compress(ByteArrayOutputStream baos) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        GzipCompressorOutputStream gcos = new GzipCompressorOutputStream(baos2);
        final byte[] buffer = new byte[2048];
        int n;
        while (-1 != (n = bais.read(buffer))) {
            gcos.write(buffer, 0, n);
        }
        gcos.close();
        bais.close();
        return baos2;
    }

    private String getFormattedDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("YYYYMMdd"));
    }

    private synchronized Integer getNextIncrement() {
        Optional<EnveloppeCounter> optEnveloppeCounter = enveloppeCounterRepository.findByDate(LocalDate.now());
        if (optEnveloppeCounter.isPresent()) {
            EnveloppeCounter enveloppeCounter = optEnveloppeCounter.get();
            LOGGER.debug("Reusing counter for {} ({})", enveloppeCounter.getDate(), enveloppeCounter.getCounter());
            Integer current = enveloppeCounter.getCounter();
            enveloppeCounter.setCounter(current + 1);
            enveloppeCounterRepository.save(enveloppeCounter);
            return current + 1;
        } else {
            LOGGER.debug("Creating new counter for {}", LocalDate.now());
            EnveloppeCounter enveloppeCounter = new EnveloppeCounter(LocalDate.now(), 1);
            enveloppeCounterRepository.save(enveloppeCounter);
            return 1;
        }
    }

    @Override
    public void onApplicationEvent(@NotNull ActeHistoryEvent event) {
        switch (event.getActeHistory().getStatus()) {
            case CREATED: createArchive(event.getActeHistory().getActeUuid()); break;
            case ARCHIVE_CREATED: checkArchiveSize(event.getActeHistory()); break;
            case CANCELLATION_ASKED: createCancellationMessage(event.getActeHistory().getActeUuid()); break;
            case CANCELLATION_ARCHIVE_CREATED: checkArchiveSize(event.getActeHistory()); break;
        }
    }
}

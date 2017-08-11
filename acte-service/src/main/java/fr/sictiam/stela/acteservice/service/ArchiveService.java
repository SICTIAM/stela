package fr.sictiam.stela.acteservice.service;

import fr.sictiam.stela.acteservice.dao.ActeHistoryRepository;
import fr.sictiam.stela.acteservice.dao.ActeRepository;
import fr.sictiam.stela.acteservice.model.*;
import fr.sictiam.stela.acteservice.model.event.ActeEvent;
import fr.sictiam.stela.acteservice.model.xml.*;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

@Service
public class ArchiveService implements ApplicationListener<ActeEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveService.class);

    private String departement = "006";
    private String arrondissement = "2";
    private String siren = "210600730";
    private String trigraph = "SIC";

    private final ActeRepository acteRepository;
    private final ActeHistoryRepository acteHistoryRepository;
    private final Jaxb2Marshaller jaxb2Marshaller;
    private final ApplicationEventPublisher applicationEventPublisher;

    public ArchiveService(ActeRepository acteRepository, ActeHistoryRepository acteHistoryRepository,
                          Jaxb2Marshaller jaxb2Marshaller, ApplicationEventPublisher applicationEventPublisher) {
        this.acteRepository = acteRepository;
        this.acteHistoryRepository = acteHistoryRepository;
        this.jaxb2Marshaller = jaxb2Marshaller;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * Compress file and annexes into a tar.gz archive.
     */
    public void createArchive(Acte acte) {
        try {
            int deliveryNumber = new Random().nextInt(10000);

            String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYYMMdd"));

            // this is the base filename for the message and attachments
            String baseFilename = String.format("%s-%s-%s-%s-%s-%s-%s",
                    departement,
                    siren,
                    today,
                    acte.getNumber(),
                    acte.getNature().getAbbreviation(),
                    Flux.TRANSMISSION_ACTE.getTransactionNumber(),
                    Flux.TRANSMISSION_ACTE.getFluxNumber());

            Map<String, byte[]> annexes = new HashMap<>();

            String acteFilename =
                    String.format("CO_DE-%s_%d.%s", baseFilename, 1, StringUtils.getFilenameExtension(acte.getFilename()));

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

            String enveloppeName = String.format("EACT--%s--%s-%d.xml", siren, today, deliveryNumber);
            JAXBElement<DonneesEnveloppeCLMISILL> donneesEnveloppeCLMISILL1 = generateEnveloppe(acte, messageFilename);
            String enveloppeContent = marshalToString(donneesEnveloppeCLMISILL1);

            String archiveName = String.format("%s-%s.%s",
                    trigraph,
                    StringUtils.stripFilenameExtension(enveloppeName),
                    "tar.gz");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);

            addEntry(enveloppeName, enveloppeContent.getBytes(), taos);
            addEntry(messageFilename, messageContent.getBytes(), taos);
            addEntry(acteFilename, acte.getFile(), taos);
            for (String annexeName : annexes.keySet()) {
                addEntry(annexeName, annexes.get(annexeName), taos);
            }

            taos.close();
            baos.close();

            ByteArrayOutputStream baos2 = compress(baos);

            // TODO better store the archive with the history trace
            LocalDateTime now = LocalDateTime.now();
            byte[] archiveData = baos2.toByteArray();
            acte.setArchive(archiveData);
            acte.setArchiveName(archiveName);
            acte.setLastUpdateTime(now);
            acte.setStatus(StatusType.ARCHIVE_CREATED);
            acteRepository.save(acte);

            acteHistoryRepository.save(new ActeHistory(acte.getUuid(), StatusType.ARCHIVE_CREATED, now, null));

            applicationEventPublisher.publishEvent(new ActeEvent(this, acte, StatusType.ARCHIVE_CREATED));

            LOGGER.info("Archive created : {}", archiveName);
        } catch (Exception e) {
            LOGGER.error("Error while generating archive for acte {} : {}", acte.getNumber(), e.getMessage());
            acteHistoryRepository.save(new ActeHistory(acte.getUuid(), StatusType.FILE_ERROR, LocalDateTime.now(), e.getMessage()));
        }
    }

    public void createCancellationMessage(Acte acte) {
        Annulation annulation = new Annulation();
        annulation.setIDActe(acte.getNumber());

        // TODO : finish XML data generation, update status and publish event
    }

    private JAXBElement<DonneesEnveloppeCLMISILL> generateEnveloppe(Acte acte, String messageFilename) {

        IDCL idcl = new IDCL();
        idcl.setDepartement(departement);
        idcl.setArrondissement(arrondissement);
        idcl.setNature(acte.getNature().getCode());
        idcl.setSIREN(siren);

        Referent referent = new Referent();
        // TODO extract to local authority config
        referent.setEmail("servicedemat@sictiam.fr");
        referent.setNom("Demat SICTIAM");
        referent.setTelephone("0101010101");

        DonneesEnveloppeCLMISILL.Emetteur emetteur = new DonneesEnveloppeCLMISILL.Emetteur();
        emetteur.setIDCL(idcl);
        emetteur.setReferent(referent);

        DonneesEnveloppeCLMISILL.AdressesRetour adressesRetour = new DonneesEnveloppeCLMISILL.AdressesRetour();
        // TODO extract to local authority config
        adressesRetour.getEmail().add("servidemat@sictiam.fr");
        adressesRetour.getEmail().add("dev@sictiam.fr");

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
        // TODO set the correct values for code matieres data
        DonneesActe.CodeMatiere1 codeMatiere1 = new DonneesActe.CodeMatiere1();
        codeMatiere1.setCodeMatiere(1);
        donneesActe.setCodeMatiere1(codeMatiere1);
        DonneesActe.CodeMatiere2 codeMatiere2 = new DonneesActe.CodeMatiere2();
        codeMatiere2.setCodeMatiere(2);
        donneesActe.setCodeMatiere2(codeMatiere2);
        donneesActe.setCodeNatureActe(Integer.valueOf(acte.getNature().getCode()));
        donneesActe.setDate(acte.getDecision());
        donneesActe.setNumeroInterne(acte.getNumber());
        // TODO what is it ?
        donneesActe.setClassificationDateVersion(acte.getDecision());
        // TODO what is it ?
        donneesActe.setObjet("");

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

    @Override
    public void onApplicationEvent(@NotNull ActeEvent event) {
        switch (event.getStatus()) {
            case CREATED: createArchive(event.getActe());
            case TO_CANCEL: createCancellationMessage(event.getActe());
        }
    }
}

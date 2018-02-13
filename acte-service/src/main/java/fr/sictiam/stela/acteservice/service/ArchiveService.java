package fr.sictiam.stela.acteservice.service;

import fr.sictiam.stela.acteservice.dao.ActeRepository;
import fr.sictiam.stela.acteservice.dao.AdminRepository;
import fr.sictiam.stela.acteservice.dao.EnveloppeCounterRepository;
import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.model.ActeHistory;
import fr.sictiam.stela.acteservice.model.Admin;
import fr.sictiam.stela.acteservice.model.Attachment;
import fr.sictiam.stela.acteservice.model.EnveloppeCounter;
import fr.sictiam.stela.acteservice.model.Flux;
import fr.sictiam.stela.acteservice.model.LocalAuthority;
import fr.sictiam.stela.acteservice.model.StatusType;
import fr.sictiam.stela.acteservice.model.event.ActeHistoryEvent;
import fr.sictiam.stela.acteservice.model.xml.*;
import fr.sictiam.stela.acteservice.service.exceptions.ActeNotFoundException;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import xyz.capybara.clamav.ClamavClient;
import xyz.capybara.clamav.commands.scan.result.ScanResult;
import xyz.capybara.clamav.commands.scan.result.ScanResult.OK;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import javax.xml.bind.JAXBElement;
import javax.xml.transform.stream.StreamResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class ArchiveService implements ApplicationListener<ActeHistoryEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveService.class);

    @Value("${application.archive.maxSize}")
    private Integer archiveMaxSize;

    @Value("${application.clamav.port}")
    private Integer clamavPort;

    @Value("${application.clamav.host}")
    private String clamavHost;

    private String trigraph = "SIC";

    private final ActeRepository acteRepository;
    private final Jaxb2Marshaller jaxb2Marshaller;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final EnveloppeCounterRepository enveloppeCounterRepository;
    private final AdminRepository adminRepository;
    private ClamavClient clamavClient;

    public ArchiveService(ActeRepository acteRepository, Jaxb2Marshaller jaxb2Marshaller,
            ApplicationEventPublisher applicationEventPublisher, EnveloppeCounterRepository enveloppeCounterRepository,
            AdminRepository adminRepository) {
        this.acteRepository = acteRepository;
        this.jaxb2Marshaller = jaxb2Marshaller;
        this.applicationEventPublisher = applicationEventPublisher;
        this.enveloppeCounterRepository = enveloppeCounterRepository;
        this.adminRepository = adminRepository;
    }

    @PostConstruct
    private void init() {
        clamavClient = new ClamavClient(clamavHost, clamavPort);
    }

    private void checkAntivirus(String acteUuid) {

        Acte acte = acteRepository.findByUuidAndDraftNull(acteUuid).orElseThrow(ActeNotFoundException::new);
        Attachment mainAttachment = acte.getActeAttachment();
        ScanResult mainResult = clamavClient.scan(new ByteArrayInputStream(mainAttachment.getFile()));
        StatusType status = StatusType.ANTIVIRUS_OK;
        if (!mainResult.equals(OK.INSTANCE)) {
            status = StatusType.ANTIVIRUS_KO;
        }
        for (Attachment attachment : acte.getAnnexes()) {
            ScanResult attachmentResult = clamavClient.scan(new ByteArrayInputStream(attachment.getFile()));
            if (!attachmentResult.equals(OK.INSTANCE)) {
                status = StatusType.ANTIVIRUS_KO;
            }
        }
        applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, new ActeHistory(acteUuid, status)));
    }

    /**
     * Compress file and annexes into a tar.gz archive.
     */
    private void createArchive(String acteUuid) {
        Acte acte = acteRepository.findByUuidAndDraftNull(acteUuid).orElseThrow(ActeNotFoundException::new);

        try {
            int deliveryNumber = getNextIncrement();

            // this is the base filename for the message and attachments
            String baseFilename = getBaseFilename(acte, Flux.TRANSMISSION_ACTE);

            String acteFilename = String.format("%s-%s_%d.%s",
                    acte.getActeAttachment().getAttachmentType() != null
                            ? acte.getActeAttachment().getAttachmentType().getCode()
                            : "CO_DE",
                    baseFilename, 1, StringUtils.getFilenameExtension(acte.getActeAttachment().getFilename()));

            Map<String, byte[]> annexes = new HashMap<>();
            acte.getAnnexes().forEach(attachment -> {
                // sequence 1 is taken by the Acte file, so we start at two
                int sequence = annexes.size() + 2;
                String tempFilename = String.format("%s-%s_%d.%s",
                        attachment.getAttachmentType() != null ? attachment.getAttachmentType().getCode() : "CO_DE",
                        baseFilename, sequence, StringUtils.getFilenameExtension(attachment.getFilename()));
                annexes.put(tempFilename, attachment.getFile());
            });

            String messageFilename = String.format("%s_%d.xml", baseFilename, 0);
            JAXBElement<DonneesActe> donneesActe = generateDonneesActe(acte, acteFilename, annexes.keySet());
            String messageContent = marshalToString(donneesActe);

            String enveloppeName = String.format("EACT--%s--%s-%d.xml", acte.getLocalAuthority().getSiren(),
                    getFormattedDate(LocalDate.now()), deliveryNumber);
            JAXBElement<DonneesEnveloppeCLMISILL> donneesEnveloppeCLMISILL1 = generateEnveloppe(
                    acte.getLocalAuthority(), messageFilename);
            String enveloppeContent = marshalToString(donneesEnveloppeCLMISILL1);

            String archiveName = getArchiveName(enveloppeName);

            ByteArrayOutputStream baos = createArchiveAndCompress(enveloppeName, enveloppeContent, messageFilename,
                    messageContent, acte.getActeAttachment().getFile(), acteFilename, annexes);

            ActeHistory acteHistory = new ActeHistory(acte.getUuid(), StatusType.ARCHIVE_CREATED, LocalDateTime.now(),
                    baos.toByteArray(), archiveName);

            applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory));

            LOGGER.info("Archive created : {}", archiveName);
        } catch (Exception e) {
            LOGGER.error("Error while generating archive for acte {} : {}", acte.getNumber(), e.getMessage());
            ActeHistory acteHistory = new ActeHistory(acte.getUuid(), StatusType.FILE_ERROR, e.getMessage());
            applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory));
        }
    }

    private String generateIdActe(Acte acte) {
        return String.format("%s-%s-%s-%s-%s", acte.getLocalAuthority().getDepartment(),
                acte.getLocalAuthority().getSiren(), acte.getDecision().format(DateTimeFormatter.ofPattern("YYYYMMdd")),
                acte.getNumber(), acte.getNature().getAbbreviation());
    }

    private void createMessageArchive(String acteUuid, Flux flux, StatusType generatedStatus) {
        Acte acte = acteRepository.findByUuidAndDraftNull(acteUuid).orElseThrow(ActeNotFoundException::new);

        try {
            LOGGER.debug("Creating cancellation message for acte {}", acteUuid);

            int deliveryNumber = getNextIncrement();

            String baseFilename = getBaseFilename(acte, flux);
            String enveloppeName = String.format("EACT--%s--%s-%d.xml", acte.getLocalAuthority().getSiren(),
                    getFormattedDate(LocalDate.now()), deliveryNumber);
            String messageFilename = String.format("%s_%d.xml", baseFilename, 0);

            Object reponse = null;
            ObjectFactory objectFactory = new ObjectFactory();

            if (Flux.ANNULATION_TRANSMISSION.equals(flux)) {
                Annulation annulation = objectFactory.createAnnulation();
                String idActe = generateIdActe(acte);
                annulation.setIDActe(idActe);
                reponse = annulation;
            } else if (Flux.AR_LETTRE_OBSERVATION.equals(flux)) {
                reponse = objectFactory.createARLettreObservations(generateDonneesCourrierPref(acte));
            } else if (Flux.AR_PIECE_COMPLEMENTAIRE.equals(flux)) {
                reponse = objectFactory.createARDemandePieceComplementaire(generateDonneesCourrierPref(acte));
            }

            StringWriter sw = new StringWriter();
            jaxb2Marshaller.marshal(reponse, new StreamResult(sw));

            String archiveName = getArchiveName(enveloppeName);

            JAXBElement<DonneesEnveloppeCLMISILL> donneesEnveloppeCLMISILL1 = generateEnveloppe(
                    acte.getLocalAuthority(), messageFilename);
            String enveloppeContent = marshalToString(donneesEnveloppeCLMISILL1);

            ByteArrayOutputStream baos = createArchiveAndCompress(enveloppeName, enveloppeContent, messageFilename,
                    sw.toString(), null, null, null);

            ActeHistory acteHistory = new ActeHistory(acte.getUuid(), generatedStatus, LocalDateTime.now(),
                    baos.toByteArray(), archiveName);

            applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory));

            LOGGER.info("Cancellation archive created : {}", archiveName);
        } catch (IOException e) {
            LOGGER.error("Error while generating archive for cancellation of acte {} : {}", acte.getNumber(),
                    e.getMessage());
            ActeHistory acteHistory = new ActeHistory(acte.getUuid(), StatusType.FILE_ERROR, e.getMessage());
            applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory));
        }
    }

    private void createMessageArchiveWithAttachment(String acteUuid, Flux flux, StatusType generatedStatus,
            Attachment attachment) {
        Acte acte = acteRepository.findByUuidAndDraftNull(acteUuid).orElseThrow(ActeNotFoundException::new);

        try {
            int deliveryNumber = getNextIncrement();

            String baseFilename = getBaseFilename(acte, flux);

            String repFilename = String.format("%s_%d.%s", baseFilename, 1,
                    StringUtils.getFilenameExtension(attachment.getFilename()));

            String messageFilename = String.format("%s_%d.xml", baseFilename, 0);
            Object reponse = null;
            if (Flux.REPONSE_COURRIER_SIMPLE.equals(flux)) {
                reponse = generateReponseCourrierSimple(acte, repFilename);
            } else if (Flux.REFUS_EXPLICITE_TRANSMISSION_PIECES_COMPLEMENTAIRES.equals(flux)) {
                reponse = generateRefusPieceComplementaire(acte, repFilename);
            } else if (Flux.REFUS_EXPLICITE_LETTRE_OBSERVATION.equals(flux)) {
                reponse = generateRejetLettreObservations(acte, repFilename);
            } else if (Flux.REPONSE_LETTRE_OBSEVATION.equals(flux)) {
                reponse = generateReponseLettreObservations(acte, repFilename);
            }

            StringWriter sw = new StringWriter();
            jaxb2Marshaller.marshal(reponse, new StreamResult(sw));
            String messageContent = sw.toString();

            String enveloppeName = String.format("EACT--%s--%s-%d.xml", acte.getLocalAuthority().getSiren(),
                    getFormattedDate(LocalDate.now()), deliveryNumber);
            JAXBElement<DonneesEnveloppeCLMISILL> donneesEnveloppeCLMISILL1 = generateEnveloppe(
                    acte.getLocalAuthority(), messageFilename);
            String enveloppeContent = marshalToString(donneesEnveloppeCLMISILL1);

            String archiveName = getArchiveName(enveloppeName);

            ByteArrayOutputStream baos = createArchiveAndCompress(enveloppeName, enveloppeContent, messageFilename,
                    messageContent, attachment.getFile(), repFilename);

            ActeHistory acteHistoryCreated = new ActeHistory(acte.getUuid(), generatedStatus, LocalDateTime.now(),
                    baos.toByteArray(), archiveName);

            applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistoryCreated));

            LOGGER.info("Archive created : {}", archiveName);
        } catch (Exception e) {
            LOGGER.error("Error while generating archive for acte {} : {}", acte.getNumber(), e.getMessage());
            ActeHistory acteHistoryError = new ActeHistory(acte.getUuid(), StatusType.FILE_ERROR, e.getMessage());
            applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistoryError));
        }
    }

    public Attachment createNomenclatureAskMessage(LocalAuthority localAuthority) {

        try {
            int deliveryNumber = getNextIncrement();

            String enveloppeName = String.format("EACT--%s--%s-%d.xml", localAuthority.getSiren(),
                    getFormattedDate(LocalDate.now()), deliveryNumber);

            DemandeClassification demandeClassification = new DemandeClassification();
            demandeClassification.setDateClassification(localAuthority.getNomenclatureDate());

            StringWriter sw = new StringWriter();
            jaxb2Marshaller.marshal(demandeClassification, new StreamResult(sw));

            String archiveName = getArchiveName(enveloppeName);

            String messageFilename = String.format("%s-%s----7-1_%d.xml", localAuthority.getDepartment(),
                    localAuthority.getSiren(), deliveryNumber);

            JAXBElement<DonneesEnveloppeCLMISILL> donneesEnveloppeCLMISILL1 = generateEnveloppe(localAuthority,
                    messageFilename);
            String enveloppeContent = marshalToString(donneesEnveloppeCLMISILL1);

            ByteArrayOutputStream baos = createArchiveAndCompress(enveloppeName, enveloppeContent, messageFilename,
                    sw.toString(), null, null, null);
            Attachment attachment = new Attachment(baos.toByteArray(), archiveName, baos.size());
            return attachment;
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        return null;
    }

    private void createArchivePieceComplementaire(String acteUuid, List<Attachment> pieces) {
        Acte acte = acteRepository.findByUuidAndDraftNull(acteUuid).orElseThrow(ActeNotFoundException::new);

        try {
            int deliveryNumber = getNextIncrement();

            String baseFilename = getBaseFilename(acte, Flux.TRANSMISSION_PIECES_COMPLEMENTAIRES);

            String messageFilename = String.format("%s_%d.xml", baseFilename, 0);
            Map<String, byte[]> annexes = new HashMap<>();
            acte.getAnnexes().forEach(attachment -> {
                int sequence = annexes.size() + 1;
                String tempFilename = String.format("%s_%d.%s", baseFilename, sequence,
                        StringUtils.getFilenameExtension(attachment.getFilename()));
                annexes.put(tempFilename, attachment.getFile());
            });
            PieceComplementaire reponse = generatePieceComplementaire(acte, annexes.keySet());
            StringWriter sw = new StringWriter();
            jaxb2Marshaller.marshal(reponse, new StreamResult(sw));
            String messageContent = sw.toString();

            String enveloppeName = String.format("EACT--%s--%s-%d.xml", acte.getLocalAuthority().getSiren(),
                    getFormattedDate(LocalDate.now()), deliveryNumber);
            JAXBElement<DonneesEnveloppeCLMISILL> donneesEnveloppeCLMISILL1 = generateEnveloppe(
                    acte.getLocalAuthority(), messageFilename);
            String enveloppeContent = marshalToString(donneesEnveloppeCLMISILL1);

            String archiveName = getArchiveName(enveloppeName);

            ByteArrayOutputStream baos = createArchiveAndCompress(enveloppeName, enveloppeContent, messageFilename,
                    messageContent, null, null, annexes);

            ActeHistory acteHistoryCreated = new ActeHistory(acte.getUuid(),
                    StatusType.PIECE_COMPLEMENTAIRE_ARCHIVE_CREATED, LocalDateTime.now(), baos.toByteArray(),
                    archiveName);

            applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistoryCreated));

            LOGGER.info("Archive created : {}", archiveName);
        } catch (Exception e) {
            LOGGER.error("Error while generating archive for acte {} : {}", acte.getNumber(), e.getMessage());
            ActeHistory acteHistoryError = new ActeHistory(acte.getUuid(), StatusType.FILE_ERROR, e.getMessage());
            applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistoryError));
        }
    }

    private RefusPieceComplementaire generateRefusPieceComplementaire(Acte acte, String repFilename) {
        RefusPieceComplementaire refusPieceComplementaire = new RefusPieceComplementaire();
        refusPieceComplementaire.setIDActe(generateIdActe(acte));
        refusPieceComplementaire.setDateCourrierPref(LocalDate.now());

        FichierSigne fichierSigne = new FichierSigne();
        fichierSigne.setNomFichier(repFilename);
        refusPieceComplementaire.setDocument(fichierSigne);
        return refusPieceComplementaire;
    }

    private PieceComplementaire generatePieceComplementaire(Acte acte, Set<String> annexesFilenames) {
        PieceComplementaire pieceComplementaire = new PieceComplementaire();
        pieceComplementaire.setIDActe(generateIdActe(acte));
        pieceComplementaire.setDateCourrierPref(LocalDate.now());

        FichiersSignes fichiersSignes = new FichiersSignes();
        annexesFilenames.forEach(annexeFilename -> {
            FichierSigne fichierSigne = new FichierSigne();
            fichierSigne.setNomFichier(annexeFilename);
            fichiersSignes.getDocument().add(fichierSigne);
        });

        pieceComplementaire.setDocuments(fichiersSignes);
        return pieceComplementaire;
    }

    private RejetLettreObservations generateRejetLettreObservations(Acte acte, String repFilename) {
        RejetLettreObservations rejetLettreObservations = new RejetLettreObservations();
        rejetLettreObservations.setIDActe(generateIdActe(acte));
        rejetLettreObservations.setDateCourrierPref(LocalDate.now());

        FichierSigne fichierSigne = new FichierSigne();
        fichierSigne.setNomFichier(repFilename);
        rejetLettreObservations.setDocument(fichierSigne);
        return rejetLettreObservations;
    }

    private ReponseLettreObservations generateReponseLettreObservations(Acte acte, String repFilename) {
        ReponseLettreObservations reponseLettreObservations = new ReponseLettreObservations();
        reponseLettreObservations.setIDActe(generateIdActe(acte));
        reponseLettreObservations.setDateCourrierPref(LocalDate.now());

        FichierSigne fichierSigne = new FichierSigne();
        fichierSigne.setNomFichier(repFilename);
        reponseLettreObservations.setDocument(fichierSigne);
        return reponseLettreObservations;
    }

    private ReponseCourrierSimple generateReponseCourrierSimple(Acte acte, String fileName) {
        ReponseCourrierSimple reponseCourrierSimple = new ReponseCourrierSimple();
        reponseCourrierSimple.setIDActe(generateIdActe(acte));
        reponseCourrierSimple.setDateCourrierPref(LocalDate.now());

        FichierSigne fichierSigne = new FichierSigne();
        fichierSigne.setNomFichier(fileName);
        reponseCourrierSimple.setDocument(fichierSigne);

        return reponseCourrierSimple;
    }

    private DonneesCourrierPref generateDonneesCourrierPref(Acte acte) {
        DonneesCourrierPref donneesCourrierPref = new DonneesCourrierPref();
        donneesCourrierPref.setIDActe(generateIdActe(acte));
        donneesCourrierPref.setDateCourrierPref(LocalDate.now());
        return donneesCourrierPref;
    }

    private void checkArchiveSize(ActeHistory acteHistory) {
        LOGGER.debug("Archive size is {} (max allowed : {})", acteHistory.getFile().length, archiveMaxSize);
        if (acteHistory.getFile().length > archiveMaxSize) {
            // TODO need a specific message or is it enough with the status type ?
            ActeHistory newActeHistory = new ActeHistory(acteHistory.getActeUuid(), StatusType.ARCHIVE_TOO_LARGE);
            applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, newActeHistory));
        } else {
            ActeHistory newActeHistory = new ActeHistory(acteHistory.getActeUuid(), StatusType.ARCHIVE_SIZE_CHECKED,
                    acteHistory.getDate(), acteHistory.getFile(), acteHistory.getFileName());
            applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, newActeHistory));
        }
    }

    private String getArchiveName(String enveloppeName) {
        return String.format("%s-%s.%s", trigraph, StringUtils.stripFilenameExtension(enveloppeName), "tar.gz");
    }

    public String getBaseFilename(Acte acte, Flux flux) {
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYYMMdd"));

        return String.format("%s-%s-%s-%s-%s-%s-%s", acte.getLocalAuthority().getDepartment(),
                acte.getLocalAuthority().getSiren(), today, acte.getNumber(), acte.getNature().getAbbreviation(),
                flux.getTransactionNumber(), flux.getFluxNumber());
    }

    private JAXBElement<DonneesEnveloppeCLMISILL> generateEnveloppe(LocalAuthority localAuthority,
            String messageFilename) {

        IDCL idcl = new IDCL();
        idcl.setDepartement(localAuthority.getDepartment());
        idcl.setArrondissement(localAuthority.getDistrict());
        idcl.setNature(localAuthority.getNature());
        idcl.setSIREN(localAuthority.getSiren());

        Admin admin = adminRepository.findAll().get(0);
        Referent referent = new Referent();
        // TODO extract to local authority config
        referent.setEmail(admin.getMainEmail());
        referent.setNom("Demat SICTIAM");
        referent.setTelephone("0101010101");

        DonneesEnveloppeCLMISILL.Emetteur emetteur = new DonneesEnveloppeCLMISILL.Emetteur();
        emetteur.setIDCL(idcl);
        emetteur.setReferent(referent);

        DonneesEnveloppeCLMISILL.AdressesRetour adressesRetour = new DonneesEnveloppeCLMISILL.AdressesRetour();
        adressesRetour.getEmail().add(admin.getMainEmail());
        admin.getAdditionalEmails().forEach(eMail -> adressesRetour.getEmail().add(eMail));

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
            String messageFilename, String messageContent, byte[] acteFile, String acteFilename) throws IOException {
        return createArchiveAndCompress(enveloppeName, enveloppeContent, messageFilename, messageContent, acteFile,
                acteFilename, null);
    }

    private ByteArrayOutputStream createArchiveAndCompress(String enveloppeName, String enveloppeContent,
            String messageFilename, String messageContent, byte[] acteFile, String acteFilename,
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
        case CREATED:
            checkAntivirus(event.getActeHistory().getActeUuid());
            break;
        case ANTIVIRUS_OK:
            createArchive(event.getActeHistory().getActeUuid());
            break;
        case ARCHIVE_CREATED:
        case CANCELLATION_ARCHIVE_CREATED:
        case REJET_LETTRE_OBSERVATION_ARCHIVE_CREATED:
        case REPONSE_LETTRE_OBSERVATION_ARCHIVE_CREATED:
        case REFUS_PIECES_COMPLEMENTAIRE_ARCHIVE_CREATED:
        case PIECE_COMPLEMENTAIRE_ARCHIVE_CREATED:
        case REPONSE_COURRIER_SIMPLE_ARCHIVE_CREATED:
        case ACK_LETTRE_OBSERVATION_ARCHIVE_CREATED:
        case ACK_PIECE_COMPLEMENTAIRE_ARCHIVE_CREATED:
            checkArchiveSize(event.getActeHistory());
            break;
        case CANCELLATION_ASKED:
            createMessageArchive(event.getActeHistory().getActeUuid(), Flux.ANNULATION_TRANSMISSION,
                    StatusType.CANCELLATION_ARCHIVE_CREATED);
            break;
        case LETTRE_OBSERVATION_RECEIVED:
            createMessageArchive(event.getActeHistory().getActeUuid(), Flux.AR_LETTRE_OBSERVATION,
                    StatusType.ARCHIVE_CREATED);
            break;
        case DEMANDE_PIECE_COMPLEMENTAIRE_RECEIVED:
            createMessageArchiveWithAttachment(event.getActeHistory().getActeUuid(), Flux.AR_PIECE_COMPLEMENTAIRE,
                    StatusType.ARCHIVE_CREATED, event.getAttachments().get(0));
            break;
        case REPONSE_COURRIER_SIMPLE_ASKED:
            createMessageArchiveWithAttachment(event.getActeHistory().getActeUuid(), Flux.REPONSE_COURRIER_SIMPLE,
                    StatusType.REPONSE_COURRIER_SIMPLE_ARCHIVE_CREATED, event.getAttachments().get(0));
            break;
        case PIECE_COMPLEMENTAIRE_ASKED:
            createArchivePieceComplementaire(event.getActeHistory().getActeUuid(), event.getAttachments());
            break;
        case REFUS_PIECES_COMPLEMENTAIRE_ASKED:
            createMessageArchiveWithAttachment(event.getActeHistory().getActeUuid(),
                    Flux.REFUS_EXPLICITE_TRANSMISSION_PIECES_COMPLEMENTAIRES,
                    StatusType.REFUS_PIECES_COMPLEMENTAIRE_ARCHIVE_CREATED, event.getAttachments().get(0));
            break;
        case REPONSE_LETTRE_OBSEVATION_ASKED:
            createMessageArchiveWithAttachment(event.getActeHistory().getActeUuid(), Flux.REPONSE_LETTRE_OBSEVATION,
                    StatusType.REPONSE_LETTRE_OBSERVATION_ARCHIVE_CREATED, event.getAttachments().get(0));
            break;
        case REJET_LETTRE_OBSERVATION_ASKED:
            createMessageArchiveWithAttachment(event.getActeHistory().getActeUuid(),
                    Flux.REFUS_EXPLICITE_LETTRE_OBSERVATION, StatusType.REJET_LETTRE_OBSERVATION_ARCHIVE_CREATED,
                    event.getAttachments().get(0));
            break;
        }
    }
}

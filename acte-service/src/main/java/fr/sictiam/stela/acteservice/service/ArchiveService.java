package fr.sictiam.stela.acteservice.service;

import eu.europa.esig.dss.validation.policy.rules.Indication;
import eu.europa.esig.dss.validation.reports.DetailedReport;
import fr.sictiam.signature.utils.PadesUtils;
import fr.sictiam.signature.utils.SignatureResult;
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
import org.apache.commons.io.FilenameUtils;
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
import java.security.cert.CertificateException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ArchiveService implements ApplicationListener<ActeHistoryEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveService.class);

    @Value("${application.archive.maxSize}")
    private Integer archiveMaxSize;

    @Value("${application.clamav.port}")
    private Integer clamavPort;

    @Value("${application.clamav.host}")
    private String clamavHost;

    private final static String trigraph = "SIC";

    private final ActeRepository acteRepository;
    private final Jaxb2Marshaller jaxb2Marshaller;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final EnveloppeCounterRepository enveloppeCounterRepository;
    private final AdminRepository adminRepository;
    private final LocalesService localesService;
    private ClamavClient clamavClient;

    public ArchiveService(ActeRepository acteRepository, Jaxb2Marshaller jaxb2Marshaller,
            ApplicationEventPublisher applicationEventPublisher, EnveloppeCounterRepository enveloppeCounterRepository,
            AdminRepository adminRepository, LocalesService localesService) {
        this.acteRepository = acteRepository;
        this.jaxb2Marshaller = jaxb2Marshaller;
        this.applicationEventPublisher = applicationEventPublisher;
        this.enveloppeCounterRepository = enveloppeCounterRepository;
        this.adminRepository = adminRepository;
        this.localesService = localesService;
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
        applicationEventPublisher
                .publishEvent(new ActeHistoryEvent(this, new ActeHistory(acteUuid, status, Flux.TRANSMISSION_ACTE)));
    }

    private void checkEventAntivirus(ActeHistoryEvent event) {

        StatusType status = StatusType.ANTIVIRUS_OK;

        for (Attachment attachment : event.getAttachments()) {
            ScanResult attachmentResult = clamavClient.scan(new ByteArrayInputStream(attachment.getFile()));
            if (!attachmentResult.equals(OK.INSTANCE)) {
                status = StatusType.ANTIVIRUS_KO;
            }
        }
        applicationEventPublisher.publishEvent(new ActeHistoryEvent(this,
                new ActeHistory(event.getActeHistory().getActeUuid(), status, event.getActeHistory().getFlux()),
                event.getAttachments()));
    }

    private List<String> checkAttachmentSignature(Attachment attachment) throws CertificateException, IOException {
        List<String> messages = new ArrayList<>();
        if (FilenameUtils.isExtension(attachment.getFilename(), "pdf")) {
            DetailedReport report = PadesUtils.validatePAdESSignature(attachment.getFile());
            if (PadesUtils.isSigned(report)) {
                List<SignatureResult> signatureResults = PadesUtils.getSignatureResults(report);
                signatureResults.forEach(signatureResult -> {
                    if (!(Indication.TOTAL_PASSED.equals(signatureResult.getStatus())
                            || Indication.PASSED.equals(signatureResult.getStatus()))) {
                        LOGGER.info("DSS validation response : {}", signatureResult.getReason());
                        messages.add(localesService.getMessage("fr", "acte",
                                "$.acte.signature." + signatureResult.getReason()));
                    }
                });
            }
        }
        return messages;

    }

    private boolean checkActeSignature(String acteUuid) {
        Acte acte = acteRepository.findByUuidAndDraftNull(acteUuid).orElseThrow(ActeNotFoundException::new);
        Attachment mainAttachment = acte.getActeAttachment();
        List<String> messages = new ArrayList<>();
        try {
            messages = checkAttachmentSignature(mainAttachment);
            for (Attachment attachment : acte.getAnnexes()) {
                messages.addAll(checkAttachmentSignature(attachment));
            }
        } catch (CertificateException | IOException e) {
            LOGGER.error(e.getMessage());
        }

        if (!messages.isEmpty()) {
            String errorMessages = String.join("\n", messages);
            applicationEventPublisher.publishEvent(new ActeHistoryEvent(this,
                    new ActeHistory(acteUuid, StatusType.SIGNATURE_ERROR, errorMessages, Flux.TRANSMISSION_ACTE)));
            return false;
        } else {
            return true;
        }

    }

    private boolean checkEventSignature(ActeHistoryEvent event) {
        List<String> messages = new ArrayList<>();

        for (Attachment attachment : event.getAttachments()) {
            try {
                messages.addAll(checkAttachmentSignature(attachment));
            } catch (CertificateException | IOException e) {
                LOGGER.error(e.getMessage());
            }
        }
        if (!messages.isEmpty()) {
            String errorMessages = messages.stream().collect(Collectors.joining("\n"));
            applicationEventPublisher
                    .publishEvent(new ActeHistoryEvent(this, new ActeHistory(event.getActeHistory().getActeUuid(),
                            StatusType.SIGNATURE_ERROR, errorMessages, event.getActeHistory().getFlux())));
            return false;
        } else {
            return true;
        }
    }

    /**
     * Compress file and annexes into a tar.gz archive.
     */
    private void createArchive(String acteUuid) {
        Acte acte = acteRepository.findByUuidAndDraftNull(acteUuid).orElseThrow(ActeNotFoundException::new);
        Flux flux = Flux.TRANSMISSION_ACTE;
        try {
            int deliveryNumber = getNextIncrement();

            // this is the base filename for the message and attachments
            String baseFilename = generateBaseFilename(acte, flux);

            String acteFilename = String.format("%s-%s_%d.%s",
                    !StringUtils.isEmpty(acte.getActeAttachment().getAttachmentTypeCode())
                            ? acte.getActeAttachment().getAttachmentTypeCode()
                            : "CO_DE",
                    baseFilename, 1, StringUtils.getFilenameExtension(acte.getActeAttachment().getFilename()));

            Map<String, byte[]> annexes = new HashMap<>();
            acte.getAnnexes().forEach(attachment -> {
                // sequence 1 is taken by the Acte file, so we start at two
                int sequence = annexes.size() + 2;
                String tempFilename = String.format("%s-%s_%d.%s",
                        !StringUtils.isEmpty(attachment.getAttachmentTypeCode()) ? attachment.getAttachmentTypeCode()
                                : "CO_DE",
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
                    baos.toByteArray(), archiveName, flux);

            applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory));

            LOGGER.info("Archive created : {}", archiveName);
        } catch (Exception e) {
            LOGGER.error("Error while generating archive for acte {} : {}", acte.getNumber(), e.getMessage());
            ActeHistory acteHistory = new ActeHistory(acte.getUuid(), StatusType.FILE_ERROR, e.getMessage(), flux);
            applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory));
        }
    }

    private String generateIdActe(Acte acte) {
        return String.format("%s-%s-%s-%s-%s", acte.getLocalAuthority().getDepartment(),
                acte.getLocalAuthority().getSiren(), acte.getDecision().format(DateTimeFormatter.ofPattern("YYYYMMdd")),
                acte.getNumber(), acte.getNature().getAbbreviation());
    }

    private void createMessageArchive(String acteUuid, Flux flux) {
        Acte acte = acteRepository.findByUuidAndDraftNull(acteUuid).orElseThrow(ActeNotFoundException::new);

        try {
            LOGGER.debug("Creating {} message for acte {}", flux, acteUuid);

            int deliveryNumber = getNextIncrement();

            String baseFilename = generateBaseFilename(acte, flux);
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

            ActeHistory acteHistory = new ActeHistory(acte.getUuid(), StatusType.ARCHIVE_CREATED, LocalDateTime.now(),
                    baos.toByteArray(), archiveName, flux);

            applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory));

            LOGGER.info("Archive created : {}", archiveName);
        } catch (IOException e) {
            LOGGER.error("Error while generating archive for flux {} of acte {} : {}", flux, acte.getNumber(),
                    e.getMessage());
            ActeHistory acteHistory = new ActeHistory(acte.getUuid(), StatusType.FILE_ERROR, e.getMessage(), flux);
            applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory));
        }
    }

    private void createMessageArchiveWithAttachment(String acteUuid, Flux flux, Attachment attachment) {
        Acte acte = acteRepository.findByUuidAndDraftNull(acteUuid).orElseThrow(ActeNotFoundException::new);

        try {
            int deliveryNumber = getNextIncrement();

            String baseFilename = generateBaseFilename(acte, flux);

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

            ActeHistory acteHistoryCreated = new ActeHistory(acte.getUuid(), StatusType.ARCHIVE_CREATED,
                    LocalDateTime.now(), baos.toByteArray(), archiveName, flux);

            applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistoryCreated));

            LOGGER.info("Archive created : {}", archiveName);
        } catch (Exception e) {
            LOGGER.error("Error while generating archive for acte {} : {}", acte.getNumber(), e.getMessage());
            ActeHistory acteHistoryError = new ActeHistory(acte.getUuid(), StatusType.FILE_ERROR, e.getMessage(), flux);
            applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistoryError));
        }
    }

    public Attachment createNomenclatureAskMessage(LocalAuthority localAuthority, boolean force) {

        try {
            int deliveryNumber = getNextIncrement();

            String enveloppeName = String.format("EACT--%s--%s-%d.xml", localAuthority.getSiren(),
                    getFormattedDate(LocalDate.now()), deliveryNumber);

            DemandeClassification demandeClassification = new DemandeClassification();
            demandeClassification.setDateClassification(
                    force ? LocalDate.of(2001, Month.JANUARY, 1) : localAuthority.getNomenclatureDate());

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
        Flux flux = Flux.TRANSMISSION_PIECES_COMPLEMENTAIRES;
        try {
            int deliveryNumber = getNextIncrement();

            String baseFilename = generateBaseFilename(acte, flux);

            String messageFilename = String.format("%s_%d.xml", baseFilename, 0);
            Map<String, byte[]> annexes = new HashMap<>();
            pieces.forEach(attachment -> {
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

            ActeHistory acteHistoryCreated = new ActeHistory(acte.getUuid(), StatusType.ARCHIVE_CREATED,
                    LocalDateTime.now(), baos.toByteArray(), archiveName, flux);

            applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistoryCreated));

            LOGGER.info("Archive created : {}", archiveName);
        } catch (Exception e) {
            LOGGER.error("Error while generating archive for acte {} : {}", acte.getNumber(), e.getMessage());
            ActeHistory acteHistoryError = new ActeHistory(acte.getUuid(), StatusType.FILE_ERROR, e.getMessage(), flux);
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
            ActeHistory newActeHistory = new ActeHistory(acteHistory.getActeUuid(), StatusType.ARCHIVE_TOO_LARGE,
                    acteHistory.getFlux());
            applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, newActeHistory));
        } else {
            ActeHistory newActeHistory = new ActeHistory(acteHistory.getActeUuid(), StatusType.ARCHIVE_SIZE_CHECKED,
                    acteHistory.getDate(), acteHistory.getFile(), acteHistory.getFileName(), acteHistory.getFlux());
            applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, newActeHistory));
        }
    }

    private String getArchiveName(String enveloppeName) {
        return String.format("%s-%s.%s", trigraph, StringUtils.stripFilenameExtension(enveloppeName), "tar.gz");
    }

    public String generateMiatId(Acte acte) {
        String today = acte.getCreation().format(DateTimeFormatter.ofPattern("YYYYMMdd"));

        return String.format("%s-%s-%s-%s-%s", acte.getLocalAuthority().getDepartment(),
                acte.getLocalAuthority().getSiren(), today, acte.getNumber(), acte.getNature().getAbbreviation());
    }

    public String generateBaseFilename(Acte acte, Flux flux) {
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

        String[] codes = acte.getCode().split("-");

        Integer materialCode1 = Integer.valueOf(codes[0]);

        if (materialCode1 != 0) {
            DonneesActe.CodeMatiere1 codeMatiere1 = new DonneesActe.CodeMatiere1();
            codeMatiere1.setCodeMatiere(materialCode1);
            donneesActe.setCodeMatiere1(codeMatiere1);
        }

        Integer materialCode2 = Integer.valueOf(codes[1]);

        if (materialCode2 != 0) {
            DonneesActe.CodeMatiere2 codeMatiere2 = new DonneesActe.CodeMatiere2();
            codeMatiere2.setCodeMatiere(materialCode2);
            donneesActe.setCodeMatiere2(codeMatiere2);
        }

        Integer materialCode3 = Integer.valueOf(codes[2]);
        if (materialCode3 != 0) {
            DonneesActe.CodeMatiere3 codeMatiere3 = new DonneesActe.CodeMatiere3();
            codeMatiere3.setCodeMatiere(materialCode3);
            donneesActe.setCodeMatiere3(codeMatiere3);
        }

        Integer materialCode4 = Integer.valueOf(codes[3]);
        if (materialCode4 != 0) {
            DonneesActe.CodeMatiere4 codeMatiere4 = new DonneesActe.CodeMatiere4();
            codeMatiere4.setCodeMatiere(materialCode4);
            donneesActe.setCodeMatiere4(codeMatiere4);
        }

        Integer materialCode5 = Integer.valueOf(codes[4]);
        if (materialCode5 != 0) {
            DonneesActe.CodeMatiere5 codeMatiere5 = new DonneesActe.CodeMatiere5();
            codeMatiere5.setCodeMatiere(materialCode5);
            donneesActe.setCodeMatiere5(codeMatiere5);
        }

        donneesActe.setCodeNatureActe(Integer.valueOf(acte.getNature().getCode()));
        donneesActe.setDate(acte.getDecision());
        donneesActe.setNumeroInterne(acte.getNumber());
        donneesActe.setClassificationDateVersion(acte.getLocalAuthority().getNomenclatureDate());
        donneesActe.setObjet(acte.getObjet());
        donneesActe.setDocumentPapier(acte.isMultipleChannels() ? "O" : "N");

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
            case RECREATED:
                checkAntivirus(event.getActeHistory().getActeUuid());
                break;
            case ANTIVIRUS_OK: {
                if (Flux.TRANSMISSION_ACTE.equals(event.getActeHistory().getFlux())) {
                    if (checkActeSignature(event.getActeHistory().getActeUuid()))
                        createArchive(event.getActeHistory().getActeUuid());
                } else if (checkEventSignature(event)) {
                    if (Flux.TRANSMISSION_PIECES_COMPLEMENTAIRES.equals(event.getActeHistory().getFlux())) {
                        createArchivePieceComplementaire(event.getActeHistory().getActeUuid(), event.getAttachments());
                    } else {
                        createMessageArchiveWithAttachment(event.getActeHistory().getActeUuid(),
                                event.getActeHistory().getFlux(), event.getAttachments().get(0));
                    }
                }

                break;
            }
            case ARCHIVE_CREATED:
                checkArchiveSize(event.getActeHistory());
                break;
            case CANCELLATION_ASKED:
                createMessageArchive(event.getActeHistory().getActeUuid(), Flux.ANNULATION_TRANSMISSION);
                break;
            case LETTRE_OBSERVATION_RECEIVED:
                createMessageArchive(event.getActeHistory().getActeUuid(), Flux.AR_LETTRE_OBSERVATION);
                break;
            case DEMANDE_PIECE_COMPLEMENTAIRE_RECEIVED:
                createMessageArchive(event.getActeHistory().getActeUuid(), Flux.AR_PIECE_COMPLEMENTAIRE);
                break;
            case PIECE_COMPLEMENTAIRE_ASKED:
            case REPONSE_COURRIER_SIMPLE_ASKED:
            case REFUS_PIECES_COMPLEMENTAIRE_ASKED:
            case REPONSE_LETTRE_OBSEVATION_ASKED:
            case REJET_LETTRE_OBSERVATION_ASKED:
                checkEventAntivirus(event);
                break;
        }
    }
}

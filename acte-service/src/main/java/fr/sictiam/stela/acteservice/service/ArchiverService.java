package fr.sictiam.stela.acteservice.service;

import fr.sictiam.stela.acteservice.dao.ActeHistoryRepository;
import fr.sictiam.stela.acteservice.dao.ActeRepository;
import fr.sictiam.stela.acteservice.dao.AttachmentRepository;
import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.model.ActeHistory;
import fr.sictiam.stela.acteservice.model.Archive;
import fr.sictiam.stela.acteservice.model.ArchiveSettings;
import fr.sictiam.stela.acteservice.model.ArchiveStatus;
import fr.sictiam.stela.acteservice.model.LocalAuthority;
import fr.sictiam.stela.acteservice.model.StatusType;
import fr.sictiam.stela.acteservice.model.asalae.AsalaeDocument;
import fr.sictiam.stela.acteservice.model.asalae.AsalaeResultForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Service
public class ArchiverService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiverService.class);

    private final ActeRepository acteRepository;
    private final ActeHistoryRepository acteHistoryRepository;
    private final AttachmentRepository attachmentRepository;
    private final LocalAuthorityService localAuthorityService;
    private final RestTemplate restTemplate;

    public ArchiverService(ActeRepository acteRepository, ActeHistoryRepository acteHistoryRepository,
            AttachmentRepository attachmentRepository, LocalAuthorityService localAuthorityService,
            RestTemplate restTemplate) {
        this.acteRepository = acteRepository;
        this.acteHistoryRepository = acteHistoryRepository;
        this.attachmentRepository = attachmentRepository;
        this.localAuthorityService = localAuthorityService;
        this.restTemplate = restTemplate;
    }

    public void archiveActesTask() {
        LOGGER.info("Running archiveActesTask job...");
        List<LocalAuthority> localAuthorities = localAuthorityService.getAll();
        localAuthorities.forEach(localAuthority -> {
            if (localAuthority.getArchiveSettings() != null &&
                    localAuthority.getArchiveSettings().isArchiveActivated()) {
                LOGGER.info("Retrieving actes for localeAuthority {} ({})", localAuthority.getName(), localAuthority.getUuid());
                List<Acte> actes = acteRepository.findAllByDraftNullAndLocalAuthorityUuidAndArchiveNull(
                        localAuthority.getUuid());
                LOGGER.info("{} actes", actes.size());
                actes.forEach(acte -> {
                    if (acte.getActeHistories().last().getDate()
                            .isBefore(LocalDateTime.now().minusDays(localAuthority.getArchiveSettings().getDaysBeforeArchiving()))) {

                        archiveActe(acte, localAuthority.getArchiveSettings());
                    } else {
                        LOGGER.info("Acte {} not old enough", acte.getUuid());
                    }
                });
            } else {
                LOGGER.info("LocalAuthority {} ({}) archiving not activated", localAuthority.getName(), localAuthority.getUuid());
            }
        });
        LOGGER.info("Ending archiveActesTask job");
    }

    public void checkArchivesStatusTask() {
        LOGGER.info("Running checkArchivesStatusTask job...");
        List<LocalAuthority> localAuthorities = localAuthorityService.getAll();
        localAuthorities.forEach(localAuthority -> {
            if (localAuthority.getArchiveSettings() != null &&
                    localAuthority.getArchiveSettings().isArchiveActivated()) {
                List<Acte> actes = acteRepository.findAllByDraftNullAndLocalAuthorityUuidAndArchive_Status(
                        localAuthority.getUuid(), ArchiveStatus.SENT);
                actes.forEach(acte -> checkStatus(acte, localAuthority.getArchiveSettings()));
            }
        });
        LOGGER.info("Ending checkArchivesStatusTask job");
    }

    public void archiveActe(Acte acte, ArchiveSettings archiveSettings) {
        LOGGER.info("Archiving acte {}...", acte.getUuid());
        Optional<ActeHistory> historyAR = acte.getActeHistories().stream()
                .filter(acteHistory -> acteHistory.getStatus().equals(StatusType.ACK_RECEIVED))
                .findFirst();

        if (historyAR.isPresent() && historyAR.get().getFile() != null) {
            LOGGER.info("Creating new Pastell document");
            AsalaeDocument asalaeDocument = createAsalaeDocument(archiveSettings);
            LOGGER.info("Création: {}", asalaeDocument);

            LOGGER.info("Sending acte data to Pastell");
            AsalaeResultForm updatedAsalaeResultForm = updateAsalaeDocument(asalaeDocument, acte, archiveSettings);
            logAsalaeResultForm(updatedAsalaeResultForm);


            LOGGER.info("Sending acte file to Pastell");
            updatedAsalaeResultForm = updateFileAsalaeDocument(updatedAsalaeResultForm.getContent(), "arrete",
                    acte.getActeAttachment().getFilename(), acte.getActeAttachment().getFile(), archiveSettings);
            logAsalaeResultForm(updatedAsalaeResultForm);


            LOGGER.info("Sending ACK file to Pastell");
            updatedAsalaeResultForm = updateFileAsalaeDocument(updatedAsalaeResultForm.getContent(), "aractes",
                    historyAR.get().getFileName(), historyAR.get().getFile(), archiveSettings);
            logAsalaeResultForm(updatedAsalaeResultForm);

            LOGGER.info("Archiving Pastell document to Asalae");
            ResponseEntity<AsalaeResultForm> response = sendAction(updatedAsalaeResultForm.getContent(),
                    "send-archive", archiveSettings);
            updatedAsalaeResultForm = response.getBody();
            if (updatedAsalaeResultForm != null) {
                logAsalaeResultForm(updatedAsalaeResultForm);
            } else {
                LOGGER.error("Request result is null");
            }
            Archive archive = new Archive(asalaeDocument.getInfo().getId_d());
            archive.setStatus(response.getStatusCode().is2xxSuccessful() ? ArchiveStatus.SENT : ArchiveStatus.NOT_SENT);
            acte.setArchive(archive);
            ActeHistory acteHistory = new ActeHistory(acte.getUuid(), StatusType.SENT_TO_SAE);
            acte.getActeHistories().add(acteHistory);
            acteRepository.save(acte);
        } else {
            LOGGER.info("No AR found for this acte");
        }
    }

    private Acte checkStatus(Acte acte, ArchiveSettings archiveSettings) {
        LOGGER.info("Checking archive status for acte {}...", acte.getUuid());

        AsalaeDocument asalaeDocument = getAsalaeDocument(acte.getArchive().getAsalaeDocumentId(), archiveSettings);
        LOGGER.info("{}", asalaeDocument.toString());

        if (asalaeDocument.getAction_possible().contains("validation-sae")) {
            sendAction(asalaeDocument, "validation-sae", archiveSettings);
        }

        asalaeDocument = getAsalaeDocument(acte.getArchive().getAsalaeDocumentId(), archiveSettings);
        if (asalaeDocument.getData().containsKey("has_archive") && asalaeDocument.getData().containsKey("url_archive") &&
                !StringUtils.isEmpty(asalaeDocument.getData().get("url_archive"))) {
            acte.getArchive().setStatus(ArchiveStatus.ARCHIVED);
            acte.getArchive().setArchiveUrl((String) (asalaeDocument.getData().get("url_archive")));
            ActeHistory acteHistory = new ActeHistory(acte.getUuid(), StatusType.ACCEPTED_BY_SAE,
                    (String) (asalaeDocument.getData().get("url_archive")));
            acte.getActeHistories().add(acteHistory);
            acte = acteRepository.save(acte);
            acte = deleteActeFiles(acte);
        }
        return acte;

    }

    private void logAsalaeResultForm(AsalaeResultForm asalaeResultForm) {
        LOGGER.info("Asalae form result message: {}", asalaeResultForm.getMessage());
        LOGGER.info("Asalae form result content: {}", asalaeResultForm.getContent());
    }

    private AsalaeDocument createAsalaeDocument(ArchiveSettings archiveSettings) {
        StringBuilder url = new StringBuilder();
        url.append(archiveSettings.getPastellUrl())
                .append("/api/v2/entite/")
                .append(archiveSettings.getPastellEntity())
                .append("/document");
        LinkedMultiValueMap<String, Object> params = new LinkedMultiValueMap<String, Object>() {{
            add("type", "actes-generique");
        }};
        HttpHeaders headers = createAuthHeaders(archiveSettings.getPastellLogin(), archiveSettings.getPastellPassword());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<LinkedMultiValueMap<String, Object>> request = new HttpEntity<>(params, headers);
        ResponseEntity<AsalaeDocument> response = restTemplate.exchange(url.toString(), HttpMethod.POST, request,
                AsalaeDocument.class);
        return response.getBody();
    }

    private AsalaeDocument getAsalaeDocument(String docId, ArchiveSettings archiveSettings) {
        StringBuilder url = new StringBuilder();
        url.append(archiveSettings.getPastellUrl())
                .append("/api/v2/entite/")
                .append(archiveSettings.getPastellEntity())
                .append("/document/")
                .append(docId);
        HttpHeaders headers = createAuthHeaders(archiveSettings.getPastellLogin(), archiveSettings.getPastellPassword());
        HttpEntity<LinkedMultiValueMap<String, Object>> request = new HttpEntity<>(headers);
        ResponseEntity<AsalaeDocument> response = restTemplate.exchange(url.toString(), HttpMethod.GET, request,
                AsalaeDocument.class);
        return response.getBody();
    }

    private AsalaeResultForm updateAsalaeDocument(AsalaeDocument asalaeDocument, Acte acte,
            ArchiveSettings archiveSettings) {
        StringBuilder url = new StringBuilder();
        url.append(archiveSettings.getPastellUrl())
                .append("/api/v2/entite/")
                .append(archiveSettings.getPastellEntity())
                .append("/document/")
                .append(asalaeDocument.getInfo().getId_d());
        LinkedMultiValueMap<String, Object> acteParams = acteToPastellParams(acte);
        HttpHeaders headers = createAuthHeaders(archiveSettings.getPastellLogin(), archiveSettings.getPastellPassword());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<LinkedMultiValueMap<String, Object>> request = new HttpEntity<>(acteParams, headers);
        ResponseEntity<AsalaeResultForm> response = restTemplate.exchange(url.toString(), HttpMethod.PATCH, request,
                AsalaeResultForm.class);
        return response.getBody();
    }

    private AsalaeResultForm updateFileAsalaeDocument(AsalaeDocument asalaeDocument, String fileType, String filename,
            byte[] file, ArchiveSettings archiveSettings) {
        StringBuilder url = new StringBuilder();
        url.append(archiveSettings.getPastellUrl())
                .append("/api/v2/entite/")
                .append(archiveSettings.getPastellEntity())
                .append("/document/")
                .append(asalaeDocument.getInfo().getId_d())
                .append("/file/")
                .append(fileType);
        LinkedMultiValueMap<String, Object> params = new LinkedMultiValueMap<String, Object>() {{
            add("file_name", filename);
            add("file_content", new ByteArrayResource(file));
        }};
        HttpHeaders headers = createAuthHeaders(archiveSettings.getPastellLogin(), archiveSettings.getPastellPassword());
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<LinkedMultiValueMap<String, Object>> request = new HttpEntity<>(params, headers);
        ResponseEntity<AsalaeResultForm> response = restTemplate.exchange(url.toString(), HttpMethod.POST, request,
                AsalaeResultForm.class);
        return response.getBody();
    }

    public ResponseEntity<AsalaeResultForm> sendAction(AsalaeDocument asalaeDocument, String action,
            ArchiveSettings archiveSettings) {
        LOGGER.info("Sending action \"{}\" Asalae document {} to SAE...", action, asalaeDocument.getInfo().getId_d());
        if (asalaeDocument.getAction_possible().contains(action)) {
            StringBuilder url = new StringBuilder();
            url.append(archiveSettings.getPastellUrl())
                    .append("/api/v2/entite/")
                    .append(archiveSettings.getPastellEntity())
                    .append("/document/")
                    .append(asalaeDocument.getInfo().getId_d())
                    .append("/action/")
                    .append(action);
            HttpHeaders headers = createAuthHeaders(archiveSettings.getPastellLogin(), archiveSettings.getPastellPassword());
            HttpEntity<LinkedMultiValueMap<String, Object>> request = new HttpEntity<>(headers);
            try {
                return restTemplate.exchange(url.toString(), HttpMethod.POST, request, AsalaeResultForm.class);
            } catch (Exception e) {
                if (action.equals("validation-sae")) {
                    LOGGER.info("Received {}, archive probably not accepted yet", e.getMessage());
                } else {
                    LOGGER.error("Error while trying to send action: {}", e.getMessage());
                }
                return null;
            }
        } else {
            LOGGER.error("Action \"{}\" not available for this document, document not sent", action);
            return null;
        }
    }

    public Acte deleteActeFiles(Acte acte) {
        String acteAttachmentUuid = acte.getActeAttachment().getUuid();

        acte.setActeAttachment(null);
        acte.setAnnexes(Collections.emptyList());
        acte.getActeHistories().forEach(acteHistory -> {
            acteHistory.setFile(null);
            acteHistory.setFileName(null);
            acteHistoryRepository.save(acteHistory);
        });
        acte = acteRepository.save(acte);

        attachmentRepository.delete(attachmentRepository.findByUuid(acteAttachmentUuid).get());
        return acte;
    }

    private LinkedMultiValueMap<String, Object> acteToPastellParams(Acte acte) {
        Optional<ActeHistory> historyAR = acte.getActeHistories().stream()
                .filter(acteHistory -> acteHistory.getStatus().equals(StatusType.ACK_RECEIVED))
                .findFirst();

        LinkedMultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        // Asalae properties
        params.add("type", "actes-generique");
        params.add("envoi_sae", "true");

        // Acte Data
        params.add("acte_nature", String.valueOf(Integer.parseInt(acte.getNature().getCode())));
        params.add("numero_de_lacte", acte.getNumber());
        params.add("objet", acte.getObjet());
        params.add("classification", codeToSmallPointFormat(acte.getCode()));
        params.add("date_de_lacte", acte.getDecision().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        if (historyAR.isPresent())
            params.add("date_tdt_postage", historyAR.get().getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        return params;
    }

    private String codeToSmallPointFormat(String code) {
        String[] numbers = code.split("-");
        StringBuilder newCode = new StringBuilder(numbers[0]);
        IntStream.range(1, numbers.length)
                .forEach(idx -> {
                    if (!"0".equals(numbers[idx])) newCode.append(".").append(numbers[idx]);
                });
        return newCode.toString();
    }

    HttpHeaders createAuthHeaders(String username, String password) {
        return new HttpHeaders() {{
            String auth = String.format("%s:%s", username, password);
            String encodedAuth = new String(Base64.getEncoder().encode(auth.getBytes()));
            set("Authorization", "Basic " + encodedAuth);
        }};
    }
}

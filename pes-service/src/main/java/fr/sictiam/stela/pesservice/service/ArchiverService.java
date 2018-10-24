package fr.sictiam.stela.pesservice.service;

import fr.sictiam.stela.pesservice.dao.AttachmentRepository;
import fr.sictiam.stela.pesservice.dao.PesAllerRepository;
import fr.sictiam.stela.pesservice.dao.PesHistoryRepository;
import fr.sictiam.stela.pesservice.model.Archive;
import fr.sictiam.stela.pesservice.model.ArchiveSettings;
import fr.sictiam.stela.pesservice.model.ArchiveStatus;
import fr.sictiam.stela.pesservice.model.LocalAuthority;
import fr.sictiam.stela.pesservice.model.PesAller;
import fr.sictiam.stela.pesservice.model.PesHistory;
import fr.sictiam.stela.pesservice.model.StatusType;
import fr.sictiam.stela.pesservice.model.asalae.AsalaeDocument;
import fr.sictiam.stela.pesservice.model.asalae.AsalaeResultForm;
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
import java.util.List;
import java.util.Optional;

@Service
public class ArchiverService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiverService.class);

    private final PesAllerRepository pesAllerRepository;
    private final PesHistoryRepository pesHistoryRepository;
    private final AttachmentRepository attachmentRepository;
    private final LocalAuthorityService localAuthorityService;
    private final RestTemplate restTemplate;
    private final StorageService storageService;

    public ArchiverService(PesAllerRepository pesAllerRepository, PesHistoryRepository pesHistoryRepository,
            AttachmentRepository attachmentRepository, LocalAuthorityService localAuthorityService,
            RestTemplate restTemplate, StorageService storageService) {
        this.pesAllerRepository = pesAllerRepository;
        this.pesHistoryRepository = pesHistoryRepository;
        this.attachmentRepository = attachmentRepository;
        this.localAuthorityService = localAuthorityService;
        this.restTemplate = restTemplate;
        this.storageService = storageService;
    }

    public void archivePesTask() {
        LOGGER.info("Running archivePesTask job...");
        List<LocalAuthority> localAuthorities = localAuthorityService.getAll();
        localAuthorities.forEach(localAuthority -> {
            if (localAuthority.getArchiveSettings() != null &&
                    localAuthority.getArchiveSettings().isArchiveActivated()) {
                List<PesAller> pesAllers = pesAllerRepository.findAllByLocalAuthorityUuidAndArchiveNull(
                        localAuthority.getUuid());
                pesAllers.forEach(pesAller -> {
                    if (pesAller.getPesHistories().last().getDate()
                            .isBefore(LocalDateTime.now().minusDays(localAuthority.getArchiveSettings().getDaysBeforeArchiving()))) {

                        archivePes(pesAller, localAuthority.getArchiveSettings());
                    }
                });
            }
        });
        LOGGER.info("Ending archivePesTask job");
    }

    public void checkArchivesStatusTask() {
        LOGGER.info("Running checkArchivesStatusTask job...");
        List<LocalAuthority> localAuthorities = localAuthorityService.getAll();
        localAuthorities.forEach(localAuthority -> {
            if (localAuthority.getArchiveSettings() != null &&
                    localAuthority.getArchiveSettings().isArchiveActivated()) {
                List<PesAller> pesAllers = pesAllerRepository.findAllByLocalAuthorityUuidAndArchive_Status(
                        localAuthority.getUuid(), ArchiveStatus.SENT);
                pesAllers.forEach(pesAller -> checkStatus(pesAller, localAuthority.getArchiveSettings()));
            }
        });
        LOGGER.info("Ending checkArchivesStatusTask job");
    }

    public void archivePes(PesAller pesAller, ArchiveSettings archiveSettings) {
        Optional<PesHistory> historyAR = pesAller.getPesHistories().stream()
                .filter(pesHistory -> pesHistory.getStatus().equals(StatusType.ACK_RECEIVED))
                .findFirst();

        byte[] content;
        if (historyAR.isPresent() && (content = storageService.getAttachmentContent(historyAR.get().getAttachment())) != null) {
            LOGGER.info("Archiving pes {}...", pesAller.getUuid());
            LOGGER.info("Creating new Pastell document");
            AsalaeDocument asalaeDocument = createAsalaeDocument(archiveSettings);
            LOGGER.info("Création: {}", asalaeDocument);

            LOGGER.info("Sending pes data to Pastell");
            AsalaeResultForm updatedAsalaeResultForm = updateAsalaeDocument(asalaeDocument, pesAller, archiveSettings);
            logAsalaeResultForm(updatedAsalaeResultForm);


            LOGGER.info("Sending pes file to Pastell");
            updatedAsalaeResultForm = updateFileAsalaeDocument(updatedAsalaeResultForm.getContent(), "fichier_pes",
                    pesAller.getAttachment().getFilename(), storageService.getAttachmentContent(pesAller.getAttachment()), archiveSettings);
            logAsalaeResultForm(updatedAsalaeResultForm);

            LOGGER.info("Sending ACK file to Pastell");
            updatedAsalaeResultForm = updateFileAsalaeDocument(updatedAsalaeResultForm.getContent(), "fichier_reponse",
                    historyAR.get().getAttachment().getFilename(), content, archiveSettings);
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
            pesAller.setArchive(archive);
            PesHistory pesHistory = new PesHistory(pesAller.getUuid(), StatusType.SENT_TO_SAE);
            pesAller.getPesHistories().add(pesHistory);
            pesAllerRepository.save(pesAller);
        }
    }

    private PesAller checkStatus(PesAller pesAller, ArchiveSettings archiveSettings) {
        LOGGER.info("Checking archive status for pes {}...", pesAller.getUuid());

        AsalaeDocument asalaeDocument = getAsalaeDocument(pesAller.getArchive().getAsalaeDocumentId(), archiveSettings);
        LOGGER.info("{}", asalaeDocument.toString());

        if (asalaeDocument.getAction_possible().contains("validation-sae")) {
            sendAction(asalaeDocument, "validation-sae", archiveSettings);
        }

        asalaeDocument = getAsalaeDocument(pesAller.getArchive().getAsalaeDocumentId(), archiveSettings);
        if (asalaeDocument.getData().containsKey("has_archive") && asalaeDocument.getData().containsKey("url_archive") &&
                !StringUtils.isEmpty(asalaeDocument.getData().get("url_archive"))) {
            pesAller.getArchive().setStatus(ArchiveStatus.ARCHIVED);
            pesAller.getArchive().setArchiveUrl((String) (asalaeDocument.getData().get("url_archive")));
            PesHistory pesHistory = new PesHistory(pesAller.getUuid(), StatusType.ACCEPTED_BY_SAE,
                    (String) (asalaeDocument.getData().get("url_archive")));
            pesAller.getPesHistories().add(pesHistory);
            pesAller = pesAllerRepository.save(pesAller);
            pesAller = deletePesFiles(pesAller);
        }
        return pesAller;

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
            add("type", "helios-generique");
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

    private AsalaeResultForm updateAsalaeDocument(AsalaeDocument asalaeDocument, PesAller pesAller,
            ArchiveSettings archiveSettings) {
        StringBuilder url = new StringBuilder();
        url.append(archiveSettings.getPastellUrl())
                .append("/api/v2/entite/")
                .append(archiveSettings.getPastellEntity())
                .append("/document/")
                .append(asalaeDocument.getInfo().getId_d());
        LinkedMultiValueMap<String, Object> pesParams = pesToPastellParams(pesAller);
        HttpHeaders headers = createAuthHeaders(archiveSettings.getPastellLogin(), archiveSettings.getPastellPassword());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<LinkedMultiValueMap<String, Object>> request = new HttpEntity<>(pesParams, headers);
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

    public PesAller deletePesFiles(PesAller pesAller) {
        String pesAttachmentUuid = pesAller.getAttachment().getUuid();

        pesAller.setAttachment(null);

        pesAller.getPesHistories().forEach(pesHistory -> {
            // TODO : delete files on storage driver
            pesHistory.setAttachment(null);
            pesHistoryRepository.save(pesHistory);
        });
        pesAller = pesAllerRepository.save(pesAller);

        attachmentRepository.delete(attachmentRepository.findByUuid(pesAttachmentUuid).get());
        return pesAller;
    }

    private LinkedMultiValueMap<String, Object> pesToPastellParams(PesAller pesAller) {
        Optional<PesHistory> historyAR = pesAller.getPesHistories().stream()
                .filter(pesHistory -> pesHistory.getStatus().equals(StatusType.ACK_RECEIVED))
                .findFirst();

        LinkedMultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        // Asalae properties
        params.add("type", "helios-generique");
        params.add("envoi_sae", "true");

        // Pes Data
        params.add("objet", pesAller.getObjet());
        params.add("id_coll", pesAller.getColCode());
        params.add("cod_bud", pesAller.getBudCode());

        if (historyAR.isPresent())
            params.add("date_tdt_postage", historyAR.get().getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        return params;
    }

    HttpHeaders createAuthHeaders(String username, String password) {
        return new HttpHeaders() {{
            String auth = String.format("%s:%s", username, password);
            String encodedAuth = new String(Base64.getEncoder().encode(auth.getBytes()));
            set("Authorization", "Basic " + encodedAuth);
        }};
    }
}

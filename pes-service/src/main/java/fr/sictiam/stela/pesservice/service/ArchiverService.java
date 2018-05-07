package fr.sictiam.stela.pesservice.service;

import fr.sictiam.stela.pesservice.dao.AttachmentRepository;
import fr.sictiam.stela.pesservice.dao.PesAllerRepository;
import fr.sictiam.stela.pesservice.dao.PesHistoryRepository;
import fr.sictiam.stela.pesservice.model.ArchiveSettings;
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

    public ArchiverService(PesAllerRepository pesAllerRepository, PesHistoryRepository pesHistoryRepository,
            AttachmentRepository attachmentRepository, LocalAuthorityService localAuthorityService,
            RestTemplate restTemplate) {
        this.pesAllerRepository = pesAllerRepository;
        this.pesHistoryRepository = pesHistoryRepository;
        this.attachmentRepository = attachmentRepository;
        this.localAuthorityService = localAuthorityService;
        this.restTemplate = restTemplate;
    }


    public void archiveActesTask() {
        List<LocalAuthority> localAuthorities = localAuthorityService.getAll();
        localAuthorities.forEach(localAuthority -> {
            if (localAuthority.getArchiveSettings() != null &&
                    localAuthority.getArchiveSettings().isArchiveActivated()) {
                List<PesAller> pesAllers = pesAllerRepository.findAllByLocalAuthorityUuidAndArchivedFalse(
                        localAuthority.getUuid());
                pesAllers.forEach(pesAller -> {
                    if (pesAller.getPesHistories().last().getDate().isBefore(LocalDateTime.now().minusMonths(2))) {
                        archivePes(pesAller, localAuthority.getArchiveSettings());
                        pesAller = deletePesFile(pesAller);
                        pesAller.setArchived(true);
                        pesAllerRepository.save(pesAller);
                    }
                });
            }
        });
    }

    public void archivePes(PesAller pesAller, ArchiveSettings archiveSettings) {

        LOGGER.info("Creating new Pastell document");
        AsalaeDocument asalaeDocument = createAsalaeDocument(archiveSettings);
        LOGGER.info("Création: {}", asalaeDocument);

        LOGGER.info("Sending pes data to Pastell");
        AsalaeResultForm updatedAsalaeResultForm = updateAsalaeDocument(asalaeDocument, pesAller, archiveSettings);
        logAsalaeResultForm(updatedAsalaeResultForm);


        LOGGER.info("Sending pes file to Pastell");
        updatedAsalaeResultForm = updateFileAsalaeDocument(updatedAsalaeResultForm.getContent(), "arrete",
                pesAller.getAttachment().getFilename(), pesAller.getAttachment().getFile(), archiveSettings);
        logAsalaeResultForm(updatedAsalaeResultForm);

        Optional<PesHistory> historyAR = pesAller.getPesHistories().stream()
                .filter(pesHistory -> pesHistory.getStatus().equals(StatusType.ACK_RECEIVED))
                .findFirst();
        if (historyAR.isPresent()) {
            LOGGER.info("Sending ACK file to Pastell");
            updatedAsalaeResultForm = updateFileAsalaeDocument(updatedAsalaeResultForm.getContent(), "aractes",
                    historyAR.get().getFileName(), historyAR.get().getFile(), archiveSettings);
            logAsalaeResultForm(updatedAsalaeResultForm);
        }

        LOGGER.info("Archiving Pastell document to Asalae");
        updatedAsalaeResultForm = sendAsalaeDocumentToSAE(updatedAsalaeResultForm.getContent(), "send-archive",
                archiveSettings);
        if (updatedAsalaeResultForm != null) {
            logAsalaeResultForm(updatedAsalaeResultForm);
        } else {
            LOGGER.error("Request result is null");
        }
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

    private AsalaeResultForm updateAsalaeDocument(AsalaeDocument asalaeDocument, PesAller pesAller,
            ArchiveSettings archiveSettings) {
        StringBuilder url = new StringBuilder();
        url.append(archiveSettings.getPastellUrl())
                .append("/api/v2/entite/")
                .append(archiveSettings.getPastellEntity())
                .append("/document/")
                .append(asalaeDocument.getInfo().getId_d());
        LinkedMultiValueMap<String, Object> acteParams = pesToPastellParams(pesAller);
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

    public AsalaeResultForm sendAsalaeDocumentToSAE(AsalaeDocument asalaeDocument, String action,
            ArchiveSettings archiveSettings) {
        LOGGER.info("Sending Asalae document {} to SAE...", asalaeDocument.getInfo().getId_d());
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
            ResponseEntity<AsalaeResultForm> response = restTemplate.exchange(url.toString(), HttpMethod.POST, request,
                    AsalaeResultForm.class);
            return response.getBody();
        } else {
            LOGGER.error("Action \"send-archive\" not available for this document, document not sent");
            return null;
        }
    }

    public PesAller deletePesFile(PesAller pesAller) {
        String pesAttachmentUuid = pesAller.getAttachment().getUuid();

        pesAller.setAttachment(null);
        pesAller.getPesHistories().forEach(pesHistory -> {
            pesHistory.setFile(null);
            pesHistory.setFileName(null);
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
        params.add("type", "actes-generique");
        params.add("envoi_sae", "true");

        // PES Data
        // TODO

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

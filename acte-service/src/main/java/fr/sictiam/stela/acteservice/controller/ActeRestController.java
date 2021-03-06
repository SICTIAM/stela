package fr.sictiam.stela.acteservice.controller;

import com.lowagie.text.DocumentException;
import fr.sictiam.stela.acteservice.model.*;
import fr.sictiam.stela.acteservice.model.ui.ActeCSVUI;
import fr.sictiam.stela.acteservice.model.ui.ActeUI;
import fr.sictiam.stela.acteservice.model.ui.ActeUuidsAndSearchUI;
import fr.sictiam.stela.acteservice.model.ui.SearchResultsUI;
import fr.sictiam.stela.acteservice.service.ActeService;
import fr.sictiam.stela.acteservice.service.LocalAuthorityService;
import fr.sictiam.stela.acteservice.service.exceptions.FileNotFoundException;
import fr.sictiam.stela.acteservice.service.util.RightUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import javax.servlet.http.HttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/acte")
public class ActeRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActeRestController.class);

    private final ActeService acteService;
    private final LocalAuthorityService localAuthorityService;

    @Autowired
    public ActeRestController(ActeService acteService, LocalAuthorityService localAuthorityService) {
        this.acteService = acteService;
        this.localAuthorityService = localAuthorityService;
    }

    @GetMapping
    public ResponseEntity<SearchResultsUI> getAll(
            @RequestParam(value = "multifield", required = false) String multifield,
            @RequestParam(value = "number", required = false) String number,
            @RequestParam(value = "objet", required = false) String objet,
            @RequestParam(value = "nature", required = false) ActeNature nature,
            @RequestParam(value = "decisionFrom", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate decisionFrom,
            @RequestParam(value = "decisionTo", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate decisionTo,
            @RequestParam(value = "status", required = false) StatusType status,
            @RequestParam(value = "limit", required = false, defaultValue = "25") Integer limit,
            @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
            @RequestParam(value = "column", required = false, defaultValue = "creation") String column,
            @RequestParam(value = "direction", required = false, defaultValue = "DESC") String direction,
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute("STELA-Current-Profile-Groups") Set<String> groups) {

        if (!RightUtils.hasRight(rights, Arrays.asList(Right.ACTES_DEPOSIT, Right.ACTES_DISPLAY))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        List<Acte> actes = acteService.getAllWithQuery(multifield, number, objet, nature, decisionFrom, decisionTo,
                status, limit, offset, column, direction, currentLocalAuthUuid, groups);
        Long count = acteService.countAllWithQuery(multifield, number, objet, nature, decisionFrom, decisionTo, status,
                currentLocalAuthUuid, groups);
        return new ResponseEntity<>(new SearchResultsUI(count, actes), HttpStatus.OK);
    }

    @PostMapping("/{uuid}/republish")
    public ResponseEntity republishActe(@RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @PathVariable String uuid) {
        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.ACTES_DEPOSIT))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        acteService.rePublishActe(uuid);
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<ActeUI> getByUuid(@RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @PathVariable String uuid) {
        if (!RightUtils.hasRight(rights, Arrays.asList(Right.ACTES_DEPOSIT, Right.ACTES_DISPLAY))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Acte acte = acteService.getByUuidAndLocalAuthorityUuid(uuid, currentLocalAuthUuid);
        boolean isActeACK = acteService.isActeACK(uuid);
        StampPosition stampPosition = localAuthorityService.getByUuid(currentLocalAuthUuid).getStampPosition();
        return new ResponseEntity<>(new ActeUI(acte, isActeACK, stampPosition), HttpStatus.OK);
    }

    @PostMapping("/ask-classification/current")
    public ResponseEntity askCurrentClassification(
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid) {
        CompletableFuture.runAsync(
                () -> acteService.askNomenclature(localAuthorityService.getByUuid(currentLocalAuthUuid), false));
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/ask-classification/{uuid}")
    public ResponseEntity askClassificationByUuid(@PathVariable String uuid) {
        CompletableFuture.runAsync(() -> acteService.askNomenclature(localAuthorityService.getByUuid(uuid), false));
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/ask-classification-force/current")
    public ResponseEntity askCurrentClassificationForce(
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin) {
        if (!isLocalAuthorityAdmin) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        CompletableFuture.runAsync(
                () -> acteService.askNomenclature(localAuthorityService.getByUuid(currentLocalAuthUuid), true));
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/ask-classification-force/{uuid}")
    public ResponseEntity askClassificationForceByUuid(@PathVariable String uuid,
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin) {
        if (!isLocalAuthorityAdmin) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        CompletableFuture.runAsync(() -> acteService.askNomenclature(localAuthorityService.getByUuid(uuid), true));
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/ask-classification/all")
    public ResponseEntity askAllClassification() {
        CompletableFuture.runAsync(acteService::askAllNomenclature);
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/{uuid}/AR_{uuid}.pdf")
    public ResponseEntity downloadACKPdf(@RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestParam(value = "disposition", required = false, defaultValue = "inline") String disposition,
            HttpServletResponse response, @PathVariable String uuid, @RequestParam(required = false) String lng) {
        if (!RightUtils.hasRight(rights, Arrays.asList(Right.ACTES_DEPOSIT, Right.ACTES_DISPLAY))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            byte[] pdf = acteService.getACKPdfs(new ActeUuidsAndSearchUI(Collections.singletonList(uuid)), lng);
            outputFile(response, pdf, "AR_" + uuid + ".pdf", disposition);
            return new ResponseEntity(HttpStatus.OK);
        } catch (IOException | DocumentException e) {
            LOGGER.error("Error while generating the ACK PDF: {}", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Hack: Not possible to have an infinite UUID list in a GET request with params
    @PostMapping("/actes.pdf")
    public ResponseEntity downloadMergedStampedAttachments(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestParam(value = "disposition", required = false, defaultValue = "inline") String disposition,
            HttpServletResponse response, @RequestBody ActeUuidsAndSearchUI acteUuidsAndSearchUI) {
        if (!RightUtils.hasRight(rights, Arrays.asList(Right.ACTES_DEPOSIT, Right.ACTES_DISPLAY))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        LocalAuthority currentLocalAuthority = localAuthorityService.getByUuid(currentLocalAuthUuid);
        try {
            byte[] pdf = acteService.getMergedStampedAttachments(acteUuidsAndSearchUI, currentLocalAuthority);
            outputFile(response, pdf,
                    "actes_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYY-MM-dd_HH-mm-ss")) + ".pdf",
                    disposition);
            return new ResponseEntity(HttpStatus.OK);
        } catch (IOException | DocumentException e) {
            LOGGER.error("Error while merging PDFs: {}", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Hack: Not possible to have an infinite UUID list in a GET request with params
    @PostMapping("/actes.zip")
    public ResponseEntity downloadZipedStampedAttachments(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestParam(value = "disposition", required = false, defaultValue = "inline") String disposition,
            HttpServletResponse response, @RequestBody ActeUuidsAndSearchUI acteUuidsAndSearchUI) {
        if (!RightUtils.hasRight(rights, Arrays.asList(Right.ACTES_DEPOSIT, Right.ACTES_DISPLAY))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        LocalAuthority currentLocalAuthority = localAuthorityService.getByUuid(currentLocalAuthUuid);
        try {
            byte[] zip = acteService.getZipedStampedAttachments(acteUuidsAndSearchUI, currentLocalAuthority);
            outputFile(response, zip,
                    "actes_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYY-MM-dd_HH-mm-ss")) + ".zip",
                    disposition);
            return new ResponseEntity(HttpStatus.OK);
        } catch (IOException | DocumentException e) {
            LOGGER.error("Error while creating zip file: {}", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Hack: Not possible to have an infinite UUID list in a GET request with params
    @PostMapping("/ARs.pdf")
    public ResponseEntity downloadACKsPdf(@RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            HttpServletResponse response, @RequestBody ActeUuidsAndSearchUI acteUuidsAndSearchUI,
            @RequestParam(value = "disposition", required = false, defaultValue = "inline") String disposition,
            @RequestParam(required = false) String lng) {
        if (!RightUtils.hasRight(rights, Arrays.asList(Right.ACTES_DEPOSIT, Right.ACTES_DISPLAY))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            byte[] pdf = acteService.getACKPdfs(acteUuidsAndSearchUI, lng);
            outputFile(response, pdf,
                    "ARs_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYY-MM-dd_HH-mm-ss")) + ".pdf",
                    disposition);
            return new ResponseEntity(HttpStatus.OK);
        } catch (DocumentException | IOException e) {
            LOGGER.error("Error while generating the ACKs PDF: {}", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Hack: Not possible to have an infinite UUID list in a GET request with params
    @PostMapping("/actes.csv")
    public ResponseEntity getCSVFromList(@RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            HttpServletResponse response, @RequestBody ActeUuidsAndSearchUI acteUuidsAndSearchUI,
            @RequestParam(required = false) String lng) {
        if (!RightUtils.hasRight(rights, Arrays.asList(Right.ACTES_DEPOSIT, Right.ACTES_DISPLAY))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        List<String> fields = ActeCSVUI.getFields();
        List<String> translatedFields = acteService.getTranslatedCSVFields(fields, lng);
        outputCSV(response, acteService.getActesCSV(acteUuidsAndSearchUI, lng).toArray(), fields, translatedFields,
                "actes.csv");
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/{uuid}/file")
    public ResponseEntity getActeAttachment(@RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            HttpServletResponse response, @PathVariable String uuid,
            @RequestParam(value = "disposition", required = false, defaultValue = "inline") String disposition) {
        if (!RightUtils.hasRight(rights, Arrays.asList(Right.ACTES_DEPOSIT, Right.ACTES_DISPLAY))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Acte acte = acteService.getByUuid(uuid);
        outputFile(response, acte.getActeAttachment().getFile(), acte.getActeAttachment().getFilename(), disposition);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/{uuid}/courrier-simple")
    public ResponseEntity sendReponseCourrierSimple(@RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @PathVariable String uuid, @RequestParam("file") MultipartFile file) {
        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.ACTES_DEPOSIT))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            acteService.sendReponseCourrierSimple(uuid, file);
        } catch (IOException e) {
            LOGGER.error("Error while trying to save file: {}", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/{uuid}/lettre-observation/{reponseOrRejet}")
    public ResponseEntity sendReponseLettreObservation(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights, @PathVariable String uuid,
            @PathVariable String reponseOrRejet, @RequestParam("file") MultipartFile file) {
        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.ACTES_DEPOSIT))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            acteService.sendReponseLettreObservation(uuid, reponseOrRejet, file);
        } catch (IOException e) {
            LOGGER.error("Error while trying to save file: {}", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/{uuid}/pieces-complementaires/{reponseOrRejet}")
    public ResponseEntity sendReponsePiecesComplementaires(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights, @PathVariable String uuid,
            @PathVariable String reponseOrRejet, @RequestParam("files") MultipartFile[] files) {
        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.ACTES_DEPOSIT))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            acteService.sendReponsePiecesComplementaires(uuid, reponseOrRejet, files);
        } catch (IOException e) {
            LOGGER.error("Error while trying to save files: {}", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/{uuid}/file/thumbnail")
    public ResponseEntity<Thumbnail> getActeAttachmentThumbnail(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights, HttpServletResponse response,
            @PathVariable String uuid) {
        if (!RightUtils.hasRight(rights, Arrays.asList(Right.ACTES_DEPOSIT, Right.ACTES_DISPLAY))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        if (StringUtils.isNotBlank(uuid)) {
            try {
                Thumbnail thumbnail = acteService.getActeAttachmentThumbnail(uuid);
                //outputFile(response, thumbnail.getImage(), "thumbnail-" + uuid + ".png", "inline");
                return new ResponseEntity<>(thumbnail, HttpStatus.OK);
            } catch (IOException e) {
                LOGGER.error("Error trying to generate the PDF's thumbnail: {}", e);
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }
    }

    @GetMapping("/{uuid}/file/stamped")
    public ResponseEntity getStampedActeAttachment(@RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestParam(value = "disposition", required = false, defaultValue = "inline") String disposition,
            HttpServletResponse response, @PathVariable String uuid, @RequestParam(required = false) Integer x,
            @RequestParam(required = false) Integer y) {
        if (!RightUtils.hasRight(rights, Arrays.asList(Right.ACTES_DEPOSIT, Right.ACTES_DISPLAY))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        LocalAuthority currentLocalAuthority = localAuthorityService.getByUuid(currentLocalAuthUuid);
        Acte acte = acteService.getByUuid(uuid);
        byte[] pdf = new byte[0];
        if (!acteService.isActeACK(uuid)) {
            pdf = acte.getActeAttachment().getFile();
        } else {
            try {
                pdf = acteService.getStampedActe(acte, x, y, currentLocalAuthority);
            } catch (IOException e) {
                LOGGER.error("Error trying to open the acte attachment PDF: {}", e);
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (DocumentException e) {
                LOGGER.error("Error trying to stamp the acte attachment PDF: {}", e);
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        outputFile(response, pdf, acte.getActeAttachment().getFilename(), disposition);
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/{uuid}/history/{historyUuid}/file")
    public ResponseEntity getFileHistory(@RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            HttpServletResponse response, @PathVariable String historyUuid,
            @RequestParam(value = "disposition", required = false, defaultValue = "inline") String disposition) {
        if (!RightUtils.hasRight(rights, Arrays.asList(Right.ACTES_DEPOSIT, Right.ACTES_DISPLAY))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        ActeHistory acteHistory = acteService.getHistoryByUuid(historyUuid);
        if (acteHistory.getFile() != null) {
            outputFile(response, acteHistory.getFile(), acteHistory.getFileName(), disposition);
            return new ResponseEntity(HttpStatus.OK);
        } else
            throw new FileNotFoundException();
    }

    @GetMapping("/{uuid}/annexes")
    public ResponseEntity<List<Attachment>> getAnnexes(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights, @PathVariable String uuid) {
        if (!RightUtils.hasRight(rights, Arrays.asList(Right.ACTES_DEPOSIT, Right.ACTES_DISPLAY))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        List<Attachment> attachments = acteService.getAnnexes(uuid);
        return new ResponseEntity<>(attachments, HttpStatus.OK);
    }

    @GetMapping("/{uuid}/annexe/{annexeUuid}")
    public ResponseEntity getAnnexe(@RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            HttpServletResponse response, @PathVariable String annexeUuid,
            @RequestParam(value = "disposition", required = false, defaultValue = "inline") String disposition) {
        if (!RightUtils.hasRight(rights, Arrays.asList(Right.ACTES_DEPOSIT, Right.ACTES_DISPLAY))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Attachment annexe = acteService.getAnnexeByUuid(annexeUuid);
        outputFile(response, annexe.getFile(), annexe.getFilename(), disposition);
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/{uuid}/annexe/{annexeUuid}/stamped")
    public ResponseEntity getStampedAnnexe(@RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            HttpServletResponse response, @PathVariable String annexeUuid, @PathVariable String uuid,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestParam(value = "disposition", required = false, defaultValue = "inline") String disposition,
            @RequestParam(required = false) Integer x, @RequestParam(required = false) Integer y) {
        if (!RightUtils.hasRight(rights, Arrays.asList(Right.ACTES_DEPOSIT, Right.ACTES_DISPLAY))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        LocalAuthority currentLocalAuthority = localAuthorityService.getByUuid(currentLocalAuthUuid);
        Acte acte = acteService.getByUuid(uuid);
        Attachment annexe = acteService.getAnnexeByUuid(annexeUuid);
        String contentType = getContentType(annexe.getFilename());
        byte[] pdf = new byte[0];
        if (!acteService.isActeACK(uuid) || !contentType.equals("application/pdf")) {
            pdf = annexe.getFile();
        } else {
            try {
                pdf = acteService.getStampedAnnexe(acte, annexe, x, y, currentLocalAuthority);
            } catch (IOException e) {
                LOGGER.error("Error trying to open the acte attachment PDF: {}", e);
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (DocumentException e) {
                LOGGER.error("Error trying to stamp the acte attachment PDF: {}", e);
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        outputFile(response, pdf, annexe.getFilename(), disposition);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/{uuid}/status/cancel")
    public ResponseEntity cancel(@RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @PathVariable String uuid) {
        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.ACTES_DEPOSIT))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        acteService.cancel(uuid);
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/attachment-types/{acteNature}/{materialCode}")
    public ResponseEntity<Set<AttachmentType>> getAttachmentTypesForNature(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @PathVariable ActeNature acteNature, @PathVariable String materialCode) {

        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.ACTES_DEPOSIT))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(
                localAuthorityService.getAttachmentTypeAvailable(acteNature, currentLocalAuthUuid, materialCode),
                HttpStatus.OK);
    }

    /* --------------------------- */
    /* ----- FILE OPERATIONS ----- */
    /* --------------------------- */

    private void outputCSV(HttpServletResponse response, Object[] beans, List<String> header,
            List<String> translatedHeader, String filename) {
        response.setHeader("Content-Disposition", String.format("inline" + "; filename=" + filename));
        response.addHeader("Content-Type", getContentType(filename) + "; charset=UTF-8");
        ICsvBeanWriter csvWriter = null;
        try {
            csvWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);

            String[] arrayTranslatedHeader = new String[translatedHeader.size()];
            translatedHeader.toArray(arrayTranslatedHeader);
            String[] arrayHeader = new String[header.size()];
            header.toArray(arrayHeader);

            csvWriter.writeHeader(arrayTranslatedHeader);
            for (Object bean : beans)
                csvWriter.write(bean, arrayHeader);
            csvWriter.close();
        } catch (Exception e) {
            LOGGER.error("Error while trying to output CSV: {}", e);
        }
    }

    private void outputFile(HttpServletResponse response, byte[] file, String filename, String disposition) {
        try {
            InputStream fileInputStream = new ByteArrayInputStream(file);

            response.setHeader("Content-Disposition", String.format(disposition + "; filename=" + filename));
            response.addHeader("Content-Type", getContentType(filename));

            IOUtils.copy(fileInputStream, response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            LOGGER.error("Error writing file to output stream. Filename was '{}'", filename, e);
        }
    }

    private String getContentType(String filename) {
        String mimeType = URLConnection.guessContentTypeFromName(filename);
        if (mimeType == null) {
            LOGGER.info("Mimetype is not detectable, will take default");
            mimeType = "application/octet-stream";
        }
        return mimeType;
    }
}

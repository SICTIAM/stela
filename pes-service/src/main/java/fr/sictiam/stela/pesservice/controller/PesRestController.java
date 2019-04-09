package fr.sictiam.stela.pesservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sictiam.stela.pesservice.model.CustomValidationUI;
import fr.sictiam.stela.pesservice.model.PesAller;
import fr.sictiam.stela.pesservice.model.PesHistory;
import fr.sictiam.stela.pesservice.model.PesRetour;
import fr.sictiam.stela.pesservice.model.Right;
import fr.sictiam.stela.pesservice.model.StatusType;
import fr.sictiam.stela.pesservice.model.ui.SearchResultsUI;
import fr.sictiam.stela.pesservice.model.util.Certificate;
import fr.sictiam.stela.pesservice.model.util.RightUtils;
import fr.sictiam.stela.pesservice.service.PesAllerService;
import fr.sictiam.stela.pesservice.service.PesRetourService;
import fr.sictiam.stela.pesservice.service.StorageService;
import fr.sictiam.stela.pesservice.service.exceptions.FileNotFoundException;
import fr.sictiam.stela.pesservice.service.exceptions.PesCreationException;
import fr.sictiam.stela.pesservice.service.util.CertUtilService;
import fr.sictiam.stela.pesservice.validation.ValidationUtil;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/pes")
public class PesRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PesRestController.class);

    private final PesAllerService pesAllerService;
    private final PesRetourService pesRetourService;
    private final CertUtilService certUtilService;
    private final StorageService storageService;

    @Value("${application.filenamepattern}")
    private String fileNamePattern;

    @Value("${application.archive.maxSize}")
    private Long maxSize;

    @Autowired
    public PesRestController(PesAllerService pesAllerService, PesRetourService pesRetourService, CertUtilService certUtilService,
            StorageService storageService) {
        this.pesAllerService = pesAllerService;
        this.pesRetourService = pesRetourService;
        this.certUtilService = certUtilService;
        this.storageService = storageService;
    }

    @GetMapping
    public ResponseEntity<SearchResultsUI> getAll(
            @RequestParam(value = "multifield", required = false) String multifield,
            @RequestParam(value = "objet", required = false) String objet,
            @RequestParam(value = "creationFrom", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate creationFrom,
            @RequestParam(value = "creationTo", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate creationTo,
            @RequestParam(value = "status", required = false) StatusType status,
            @RequestParam(value = "limit", required = false, defaultValue = "25") Integer limit,
            @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
            @RequestParam(value = "column", required = false, defaultValue = "creation") String column,
            @RequestParam(value = "direction", required = false, defaultValue = "DESC") String direction,
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid) {

        if (!RightUtils.hasRight(rights, Arrays.asList(Right.PES_DEPOSIT, Right.PES_DISPLAY))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        List<PesAller> pesList = pesAllerService.getAllWithQuery(multifield, objet, creationFrom, creationTo, status,
                limit, offset, column, direction, currentLocalAuthUuid);
        Long count = pesAllerService.countAllWithQuery(multifield, objet, creationFrom, creationTo, status,
                currentLocalAuthUuid);
        return new ResponseEntity<>(new SearchResultsUI(count, pesList), HttpStatus.OK);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<PesAller> getByUuid(
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @PathVariable String uuid) {
        if (!RightUtils.hasRight(rights, Arrays.asList(Right.PES_DEPOSIT, Right.PES_DISPLAY))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        PesAller pes = pesAllerService.getByUuidAndLocalAuthorityUuid(uuid, currentLocalAuthUuid);

        return new ResponseEntity<>(pes, HttpStatus.OK);
    }

    @GetMapping("/resend/{uuid}")
    public ResponseEntity<String> reSendFlux(@RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @PathVariable String uuid) {
        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.PES_DEPOSIT))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        pesAllerService.manualResend(uuid);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/republish/{uuid}")
    public ResponseEntity<String> rePublishFlux(@RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @PathVariable String uuid) {
        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.PES_DEPOSIT))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        pesAllerService.manualRepublish(uuid);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute(value = "STELA-Certificate", required = false) Certificate certificate,
            @RequestAttribute(value = "STELA-Current-Profile-Paired-Certificate", required = false) Certificate pairedCertificate,
            @RequestAttribute("STELA-Current-Profile-UUID") String currentProfileUuid,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestParam("pesAller") String pesAllerJson, @RequestParam("file") MultipartFile file) {
        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.PES_DEPOSIT))
                || !certUtilService.checkCert(certificate, pairedCertificate)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        if (file.getSize() > maxSize) {
            return new ResponseEntity<>("notifications.pes.sent.error.file_too_big", HttpStatus.PAYLOAD_TOO_LARGE);
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            if (pesAllerService.checkVirus(file.getBytes())) {
                LOGGER.error("PES {} attachment contains virus", pesAllerJson);
                return new ResponseEntity<>("notifications.pes.sent.virus", HttpStatus.BAD_REQUEST);
            }
            PesAller pesAller = mapper.readValue(pesAllerJson, PesAller.class);
            List<ObjectError> errors = ValidationUtil.validatePes(pesAller);
            if (!errors.isEmpty()) {
                LOGGER.error("PES {} is not valid", pesAller.getObjet());
                CustomValidationUI customValidationUI = new CustomValidationUI(errors, "has failed");
                return new ResponseEntity<>(customValidationUI, HttpStatus.BAD_REQUEST);
            }

            PesAller result = pesAllerService.create(currentProfileUuid, currentLocalAuthUuid, pesAller, file);
            return new ResponseEntity<>(result.getUuid(), HttpStatus.CREATED);

        } catch (PesCreationException e) {
            LOGGER.error("PES {}: error during creation : {}", pesAllerJson, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            LOGGER.error("PES {}: IO error during creation : {}", pesAllerJson, e.getMessage());
            throw new PesCreationException();
        }
    }

    @GetMapping("/{uuid}/file")
    public ResponseEntity getPesAttachment(
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            HttpServletResponse response, @PathVariable String uuid,
            @RequestParam(value = "disposition", required = false, defaultValue = "inline") String disposition) {
        if (!RightUtils.hasRight(rights, Arrays.asList(Right.PES_DEPOSIT, Right.PES_DISPLAY))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        PesAller pesAller = pesAllerService.getByUuidAndLocalAuthorityUuid(uuid, currentLocalAuthUuid);
        outputFile(response, storageService.getAttachmentContent(pesAller.getAttachment()), pesAller.getAttachment().getFilename(), disposition);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/{uuid}/history/{historyUuid}/file")
    public ResponseEntity getFileHistory(@RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            HttpServletResponse response, @PathVariable String historyUuid,
            @RequestParam(value = "disposition", required = false, defaultValue = "inline") String disposition) {
        if (!RightUtils.hasRight(rights, Arrays.asList(Right.PES_DEPOSIT, Right.PES_DISPLAY))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        PesHistory pesHistory = pesAllerService.getHistoryByUuid(historyUuid);
        byte[] content = storageService.getAttachmentContent(pesHistory.getAttachment());
        if (content != null) {
            outputFile(response, content, pesHistory.getAttachment().getFilename(), disposition);
            return new ResponseEntity<>(HttpStatus.OK);
        } else
            throw new FileNotFoundException();
    }

    @GetMapping("/statuses")
    public ResponseEntity<List<StatusType>> getStatuses(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights) {
        if (!RightUtils.hasRight(rights, Arrays.asList(Right.PES_DEPOSIT, Right.PES_DISPLAY))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(Arrays.asList(StatusType.values()), HttpStatus.OK);
    }

    @GetMapping("/pes-retour")
    public ResponseEntity<SearchResultsUI> getAllPesRetour(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestParam(value = "multifield", required = false) String multifield,
            @RequestParam(value = "filename", required = false) String filename,
            @RequestParam(value = "creationFrom", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate creationFrom,
            @RequestParam(value = "creationTo", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate creationTo,
            @RequestParam(value = "limit", required = false, defaultValue = "25") Integer limit,
            @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset) {
        if (!RightUtils.hasRight(rights, Arrays.asList(Right.PES_DEPOSIT, Right.PES_DISPLAY))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        List<PesRetour> pesRetours = pesRetourService.getAllWithQuery(multifield, filename, creationFrom, creationTo,
                currentLocalAuthUuid, limit, offset);
        Long count = pesRetourService.countAllWithQuery(multifield, filename, creationFrom, creationTo,
                currentLocalAuthUuid);
        return new ResponseEntity<>(new SearchResultsUI(count, pesRetours), HttpStatus.OK);
    }

    @GetMapping("/pes-retour/{uuid}/file")
    public ResponseEntity getPesRetourAttachment(@RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            HttpServletResponse response, @PathVariable String uuid,
            @RequestParam(value = "disposition", required = false, defaultValue = "inline") String disposition) {
        if (!RightUtils.hasRight(rights, Arrays.asList(Right.PES_DEPOSIT, Right.PES_DISPLAY))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        PesRetour pesRetour = pesRetourService.getByUuid(uuid);
        outputFile(response, storageService.getAttachmentContent(pesRetour.getAttachment()), pesRetour.getAttachment().getFilename(), disposition);
        return new ResponseEntity<Object>(HttpStatus.OK);
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

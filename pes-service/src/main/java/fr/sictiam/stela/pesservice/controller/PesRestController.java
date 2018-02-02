package fr.sictiam.stela.pesservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sictiam.stela.pesservice.model.PesAller;
import fr.sictiam.stela.pesservice.model.PesHistory;
import fr.sictiam.stela.pesservice.model.StatusType;
import fr.sictiam.stela.pesservice.model.ui.SearchResultsUI;
import fr.sictiam.stela.pesservice.service.LocalAuthorityService;
import fr.sictiam.stela.pesservice.service.PesAllerService;
import fr.sictiam.stela.pesservice.service.exceptions.FileNotFoundException;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/pes")
public class PesRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PesRestController.class);

    private final PesAllerService pesService;
    private final LocalAuthorityService localAuthorityService;

    @Autowired
    public PesRestController(PesAllerService pesService, LocalAuthorityService localAuthorityService) {
        this.pesService = pesService;
        this.localAuthorityService = localAuthorityService;
    }

    @GetMapping
    public ResponseEntity<SearchResultsUI> getAll(
            @RequestParam(value = "objet", required = false) String objet,
            @RequestParam(value = "creationFrom", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate creationFrom,
            @RequestParam(value = "creationTo", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate creationTo,
            @RequestParam(value = "status", required = false) StatusType status,
            @RequestParam(value = "limit", required = false, defaultValue = "25") Integer limit,
            @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
            @RequestParam(value = "column", required = false, defaultValue = "creation") String column,
            @RequestParam(value = "direction", required = false, defaultValue = "ASC") String direction,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid) {

        List<PesAller> pesList = pesService.getAllWithQuery(objet, creationFrom, creationTo, status, limit, offset, column, direction, currentLocalAuthUuid);
        Long count = pesService.countAllWithQuery(objet, creationFrom, creationTo, status, currentLocalAuthUuid);
        return new ResponseEntity<>(new SearchResultsUI(count, pesList), HttpStatus.OK);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<PesAller> getByUuid(
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @PathVariable String uuid) {
        PesAller pes = pesService.getByUuid(uuid);

        return new ResponseEntity<>(pes, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestAttribute("STELA-Current-Profile") String currentProfileUuid,
                                         @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
                                         @RequestParam("pesAller") String pesAllerJson, @RequestParam("file") MultipartFile file) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            PesAller pesAller = mapper.readValue(pesAllerJson, PesAller.class);
            PesAller result = pesService.create(currentProfileUuid, currentLocalAuthUuid, pesAller, file);
            return new ResponseEntity<>(result.getUuid(), HttpStatus.CREATED);
        } catch (IOException e) {
            LOGGER.error("IOException: Could not convert JSON to PesAller: {}", e);
            return new ResponseEntity<>("notifications.pes.sent.error.non_extractable_pes", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{uuid}/file")
    public ResponseEntity getActeAttachment(HttpServletResponse response, @PathVariable String uuid) {
        PesAller pesAller = pesService.getByUuid(uuid);
        outputFile(response, pesAller.getAttachment().getFile(), pesAller.getAttachment().getFilename());
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/{uuid}/history/{historyUuid}/file")
    public ResponseEntity getFileHistory(HttpServletResponse response, @PathVariable String historyUuid) {
        PesHistory acteHistory = pesService.getHistoryByUuid(historyUuid);
        if (acteHistory.getFile() != null) {
            outputFile(response, acteHistory.getFile(), acteHistory.getFileName());
            return new ResponseEntity(HttpStatus.OK);
        } else throw new FileNotFoundException();
    }

    private void outputFile(HttpServletResponse response, byte[] file, String filename) {
        try {
            InputStream fileInputStream = new ByteArrayInputStream(file);

            response.setHeader("Content-Disposition", String.format("inline" + "; filename=" + filename));
            response.addHeader("Content-Type", getContentType(filename) + "; charset=UTF-8");

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

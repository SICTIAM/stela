package fr.sictiam.stela.pesservice.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import fr.sictiam.stela.pesservice.model.PesAller;
import fr.sictiam.stela.pesservice.model.PesHistory;
import fr.sictiam.stela.pesservice.model.StatusType;
import fr.sictiam.stela.pesservice.model.ui.SearchResultsUI;
import fr.sictiam.stela.pesservice.service.LocalAuthorityService;
import fr.sictiam.stela.pesservice.service.PesAllerService;
import fr.sictiam.stela.pesservice.service.exceptions.FileNotFoundException;
import fr.sictiam.stela.pesservice.service.exceptions.PesCreationException;

@RestController
@RequestMapping("/api/pes")
public class PesRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PesRestController.class);

    private final PesAllerService pesService;
    private final LocalAuthorityService localAuthorityService;

    @Value("application.filenamepattern")
    private String fileNamePattern;

    @Autowired
    public PesRestController(PesAllerService pesService, LocalAuthorityService localAuthorityService) {
        this.pesService = pesService;
        this.localAuthorityService = localAuthorityService;
    }

    @GetMapping
    public ResponseEntity<SearchResultsUI> getAll(@RequestParam(value = "objet", required = false) String objet,
            @RequestParam(value = "creationFrom", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate creationFrom,
            @RequestParam(value = "creationTo", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate creationTo,
            @RequestParam(value = "status", required = false) StatusType status,
            @RequestParam(value = "limit", required = false, defaultValue = "25") Integer limit,
            @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
            @RequestParam(value = "column", required = false, defaultValue = "creation") String column,
            @RequestParam(value = "direction", required = false, defaultValue = "ASC") String direction,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid) {

        List<PesAller> pesList = pesService.getAllWithQuery(objet, creationFrom, creationTo, status, limit, offset,
                column, direction, currentLocalAuthUuid);
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

    @GetMapping("/resend/{uuid}")
    public ResponseEntity<String> reSendFlux(@PathVariable String uuid) {
        PesAller pes = pesService.getByUuid(uuid);
        pesService.send(pes);
        StatusType statusType = StatusType.MANUAL_RESENT;
        pesService.updateStatus(pes.getUuid(), statusType);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestAttribute("STELA-Current-Profile-UUID") String currentProfileUuid,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestParam("pesAller") String pesAllerJson, @RequestParam("file") MultipartFile file)
            throws PesCreationException {
        Pattern pattern = Pattern.compile(fileNamePattern);
        Matcher matcher = pattern.matcher(file.getOriginalFilename());
        if (matcher.matches()) {
            PesAller result = pesService.createFromJson(currentProfileUuid, currentLocalAuthUuid, pesAllerJson, file);
            return new ResponseEntity<>(result.getUuid(), HttpStatus.CREATED);

        } else {
            return new ResponseEntity<>("notifications.pes.sent.error.bad_file_name", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{uuid}/file")
    public ResponseEntity getPesAttachment(HttpServletResponse response, @PathVariable String uuid) {
        PesAller pesAller = pesService.getByUuid(uuid);
        outputFile(response, pesAller.getAttachment().getFile(), pesAller.getAttachment().getFilename());
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/{uuid}/history/{historyUuid}/file")
    public ResponseEntity getFileHistory(HttpServletResponse response, @PathVariable String historyUuid) {
        PesHistory pesHistory = pesService.getHistoryByUuid(historyUuid);
        if (pesHistory.getFile() != null) {
            outputFile(response, pesHistory.getFile(), pesHistory.getFileName());
            return new ResponseEntity(HttpStatus.OK);
        } else
            throw new FileNotFoundException();
    }

    @GetMapping("/statuses")
    public List<StatusType> getStatuses() {
        return Arrays.asList(StatusType.values());
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

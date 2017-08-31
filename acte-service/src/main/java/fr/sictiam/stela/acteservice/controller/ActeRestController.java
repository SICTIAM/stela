package fr.sictiam.stela.acteservice.controller;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.List;

import fr.sictiam.stela.acteservice.service.exceptions.FileNotFoundException;
import fr.sictiam.stela.acteservice.model.ActeHistory;
import fr.sictiam.stela.acteservice.model.Attachment;
import fr.sictiam.stela.acteservice.model.ui.ActeUI;
import fr.sictiam.stela.acteservice.service.ActeNotSentException;
import fr.sictiam.stela.acteservice.service.ActeService;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

import fr.sictiam.stela.acteservice.model.Acte;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/acte")
public class ActeRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActeRestController.class);

    private final ActeService acteService;

    @Autowired
    public ActeRestController(ActeService acteService){
        this.acteService = acteService;
    }

    @GetMapping
    public ResponseEntity<List<Acte>> getAll() {
        List<Acte> actes = acteService.getAll();
        return new ResponseEntity<>(actes, HttpStatus.OK);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<ActeUI> getByUuid(@PathVariable String uuid) {
        Acte acte = acteService.getByUuid(uuid);
        List<ActeHistory> acteHistory = acteService.getHistory(uuid);
        boolean isCancellable = acteService.isCancellable(uuid);
        return new ResponseEntity<>(new ActeUI(acte, acteHistory, isCancellable), HttpStatus.OK);
    }

    @GetMapping("/{uuid}/file")
    public void getFile(HttpServletResponse response, @PathVariable String uuid) {
        Acte acte = acteService.getByUuid(uuid);
        outputFile(response, acte.getFile(), acte.getFilename());
    }

    @GetMapping("/{uuid}/history")
    public ResponseEntity<List<ActeHistory>> getHistory(@PathVariable String uuid) {
        List<ActeHistory> acteHistoryList = acteService.getHistory(uuid);
        return new ResponseEntity<>(acteHistoryList, HttpStatus.OK);
    }

    @GetMapping("/{uuid}/history/{historyUuid}/file")
    public void getFileHistory(HttpServletResponse response, @PathVariable String historyUuid) {
        ActeHistory acteHistory = acteService.getHistoryByUuid(historyUuid);
        if(acteHistory.getFile() != null) {
            outputFile(response, acteHistory.getFile(), acteHistory.getFileName());
        }
        else throw new FileNotFoundException();
    }

    @GetMapping("/{uuid}/annexes")
    public ResponseEntity<List<Attachment>> getAnnexes(@PathVariable String uuid) {
        List<Attachment> attachments = acteService.getAnnexes(uuid);
        return new ResponseEntity<>(attachments, HttpStatus.OK);
    }

    @GetMapping("/{uuid}/annexe/{annexeUuid}")
    public void getAnnexe(HttpServletResponse response, @PathVariable String annexeUuid) {
        Attachment annexe = acteService.getAnnexeByUuid(annexeUuid);
        outputFile(response, annexe.getFile(), annexe.getFilename());
    }

    @PostMapping("/{uuid}/status/cancel")
    public void cancel(@PathVariable String uuid) {
        acteService.cancel(uuid);
    }

    @PostMapping
    ResponseEntity<String> create(@RequestParam("acte") String acteJson, @RequestParam("file") MultipartFile file,
                                  @RequestParam("annexes") MultipartFile... annexes) {

        ObjectMapper mapper = new ObjectMapper();
        try {
            Acte acte = mapper.readValue(acteJson, Acte.class);
            
            LOGGER.debug("Received acte : {}", acte.getTitle());
            LOGGER.debug("Received main file {} with {} annexes", file.getOriginalFilename(), annexes.length);

            Acte result = acteService.create(acte, file, annexes);
            return new ResponseEntity<>(result.getUuid(), HttpStatus.CREATED);

        } catch (IOException e) {
            LOGGER.error("IOException: Could not convert JSON to Acte: {}", e);
            return new ResponseEntity<>("notifications.acte.sent.error.non_extractable_acte", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (ActeNotSentException ns){
            LOGGER.error("ActeNotSentException: {}", ns);
            return new ResponseEntity<>("notifications.acte.sent.error.acte_not_sent", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void outputFile(HttpServletResponse response, byte[] file, String filename) {
        try {
            InputStream fileInputStream = new ByteArrayInputStream(file);

            String mimeType= URLConnection.guessContentTypeFromName(filename);
            if(mimeType==null){
                LOGGER.info("mimetype is not detectable, will take default");
                mimeType = "application/octet-stream";
            }
            response.setContentType(mimeType);
            response.setHeader("Content-Disposition", String.format("inline" + "; filename=" + filename));

            IOUtils.copy(fileInputStream, response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            LOGGER.error("Error writing file to output stream. Filename was '{}'", filename, e);
        }
    }
}

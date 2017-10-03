package fr.sictiam.stela.acteservice.controller;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.time.LocalDate;
import java.util.List;

import fr.sictiam.stela.acteservice.model.*;
import fr.sictiam.stela.acteservice.service.LocalAuthorityService;
import fr.sictiam.stela.acteservice.service.exceptions.FileNotFoundException;
import fr.sictiam.stela.acteservice.model.ui.ActeUI;
import fr.sictiam.stela.acteservice.service.ActeNotSentException;
import fr.sictiam.stela.acteservice.service.ActeService;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/acte")
public class ActeRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActeRestController.class);

    private final ActeService acteService;
    private final LocalAuthorityService localAuthorityService;

    @Autowired
    public ActeRestController(ActeService acteService, LocalAuthorityService localAuthorityService){
        this.acteService = acteService;
        this.localAuthorityService = localAuthorityService;
    }

    @GetMapping
    public ResponseEntity<List<Acte>> getAll() {
        List<Acte> actes = acteService.getAll();
        return new ResponseEntity<>(actes, HttpStatus.OK);
    }

    @GetMapping("/query")
    public ResponseEntity<List<Acte>> getAllWithQuery(
            @RequestParam(value= "number", required = false) String number,
            @RequestParam(value= "objet", required = false) String objet,
            @RequestParam(value= "nature", required = false) ActeNature nature,
            @RequestParam(value= "decisionFrom", required = false) @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate decisionFrom,
            @RequestParam(value= "decisionTo", required = false) @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate decisionTo,
            @RequestParam(value= "status", required = false) StatusType status) {
        List<Acte> actes = acteService.getAllWithQuery(number, objet, nature, decisionFrom, decisionTo, status);
        return new ResponseEntity<>(actes, HttpStatus.OK);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<ActeUI> getByUuid(@PathVariable String uuid) {
        Acte acte = acteService.getByUuid(uuid);
        boolean isCancellable = acteService.isCancellable(uuid);
        return new ResponseEntity<>(new ActeUI(acte, isCancellable), HttpStatus.OK);
    }

    @GetMapping("/{uuid}/AR.pdf")
    public void downloadACKPdf(HttpServletResponse response, @PathVariable String uuid, @RequestParam(value= "lng", required = false) String lng) {
        try {
            byte[] pdf = acteService.getACKPdf(uuid, lng);
            outputFile(response, pdf, "AR.pdf");
        } catch (Exception e) {
            LOGGER.error("Error while generating the ACK PDF: {}", e);
        }
    }

    @GetMapping("/{uuid}/file")
    public void getFile(HttpServletResponse response, @PathVariable String uuid) {
        Acte acte = acteService.getByUuid(uuid);
        outputFile(response, acte.getFile(), acte.getFilename());
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

        // TODO Retrieve current local authority
        LocalAuthority currentLocalAuthority = localAuthorityService.getByName("SICTIAM-Test").get();
        ObjectMapper mapper = new ObjectMapper();
        try {
            Acte acte = mapper.readValue(acteJson, Acte.class);
            
            LOGGER.debug("Received acte : {}", acte.getObjet());
            LOGGER.debug("Received main file {} with {} annexes", file.getOriginalFilename(), annexes.length);

            Acte result = acteService.create(currentLocalAuthority, acte, file, annexes);
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

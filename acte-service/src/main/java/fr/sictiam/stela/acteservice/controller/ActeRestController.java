package fr.sictiam.stela.acteservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URLConnection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import com.lowagie.text.DocumentException;
import fr.sictiam.stela.acteservice.model.*;
import fr.sictiam.stela.acteservice.model.ui.ActeCSVUI;
import fr.sictiam.stela.acteservice.model.ui.ActeUI;
import fr.sictiam.stela.acteservice.model.ui.ActeUuidsAndSearchUI;
import fr.sictiam.stela.acteservice.model.ui.CustomValidationUI;
import fr.sictiam.stela.acteservice.service.ActeService;
import fr.sictiam.stela.acteservice.service.LocalAuthorityService;
import fr.sictiam.stela.acteservice.service.exceptions.ActeNotSentException;
import fr.sictiam.stela.acteservice.service.exceptions.FileNotFoundException;
import fr.sictiam.stela.acteservice.service.exceptions.NoContentException;
import fr.sictiam.stela.acteservice.validation.ValidationUtil;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

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
    public ResponseEntity<List<Acte>> getAll(
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
        boolean isActeACK = acteService.isActeACK(uuid);
        // TODO Retrieve current local authority
        StampPosition stampPosition = localAuthorityService.getByName("SICTIAM-Test").get().getStampPosition();
        return new ResponseEntity<>(new ActeUI(acte, isActeACK, stampPosition), HttpStatus.OK);
    }

    @GetMapping("/{uuid}/AR_{uuid}.pdf")
    public ResponseEntity downloadACKPdf(HttpServletResponse response, @PathVariable String uuid, @RequestParam(required = false) String lng) {
        try {
            byte[] pdf = acteService.getACKPdfs(new ActeUuidsAndSearchUI(Collections.singletonList(uuid)), lng);
            outputFile(response, pdf, "AR_" + uuid + ".pdf");
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error("Error while generating the ACK PDF: {}", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Hack: Not possible to have an infinite UUID list in a GET request with params
    @PostMapping("/actes.pdf")
    public ResponseEntity downloadMergedStampedAttachments(HttpServletResponse response, @RequestBody ActeUuidsAndSearchUI acteUuidsAndSearchUI) {
        // TODO Retrieve current local authority
        LocalAuthority currentLocalAuthority = localAuthorityService.getByName("SICTIAM-Test").get();
        try {
            byte[] pdf = acteService.getMergedStampedAttachments(acteUuidsAndSearchUI, currentLocalAuthority);
            outputFile(response, pdf, "actes_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYY-MM-dd_HH-mm-ss")) + ".pdf");
            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error("Error while merging PDFs: {}", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Hack: Not possible to have an infinite UUID list in a GET request with params
    @PostMapping("/actes.zip")
    public ResponseEntity downloadZipedStampedAttachments(HttpServletResponse response, @RequestBody ActeUuidsAndSearchUI acteUuidsAndSearchUI) {
        // TODO Retrieve current local authority
        LocalAuthority currentLocalAuthority = localAuthorityService.getByName("SICTIAM-Test").get();
        try {
            byte[] zip = acteService.getZipedStampedAttachments(acteUuidsAndSearchUI, currentLocalAuthority);
            outputFile(response, zip, "actes_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYY-MM-dd_HH-mm-ss")) + ".zip");
            return new ResponseEntity(HttpStatus.OK);
        } catch (NoContentException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("Error while creating zip file: {}", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Hack: Not possible to have an infinite UUID list in a GET request with params
    @PostMapping("/ARs.pdf")
    public ResponseEntity downloadACKsPdf(HttpServletResponse response, @RequestBody ActeUuidsAndSearchUI acteUuidsAndSearchUI, @RequestParam(required = false) String lng) {
        try {
            byte[] pdf = acteService.getACKPdfs(acteUuidsAndSearchUI, lng);
            outputFile(response, pdf, "ARs_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYY-MM-dd_HH-mm-ss")) + ".pdf");
            return new ResponseEntity(HttpStatus.OK);
        } catch (NoContentException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("Error while generating the ACKs PDF: {}", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Hack: Not possible to have an infinite UUID list in a GET request with params 
    @PostMapping("/actes.csv")
    public ResponseEntity getCSVFromList(HttpServletResponse response, @RequestBody ActeUuidsAndSearchUI acteUuidsAndSearchUI, @RequestParam(required = false) String lng) {
        List<String> fields = ActeCSVUI.getFields();
        List<String> translatedFields = acteService.getTranslatedCSVFields(fields, lng);
        outputCSV(response, acteService.getActesCSV(acteUuidsAndSearchUI, lng).toArray(), fields, translatedFields, "actes.csv");
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/{uuid}/file")
    public ResponseEntity getActeAttachment(HttpServletResponse response, @PathVariable String uuid) {
        Acte acte = acteService.getByUuid(uuid);
        outputFile(response, acte.getActeAttachment().getFile(), acte.getActeAttachment().getFilename());
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/{uuid}/file/thumbnail")
    public ResponseEntity getActeAttachmentThumbnail(HttpServletResponse response, @PathVariable String uuid) {
        if(StringUtils.isNotBlank(uuid)) {
            try {
                byte[] thumbnail = acteService.getActeAttachmentThumbnail(uuid);
                outputFile(response, thumbnail, "thumbnail-" + uuid + ".png");
                return new ResponseEntity(HttpStatus.OK);
            } catch (IOException e) {
                LOGGER.error("Error trying to generate the PDF's thumbnail: {}", e);
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        else {
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }
    }

    @GetMapping("/{uuid}/file/stamped")
    public ResponseEntity getStampedActeAttachment(HttpServletResponse response, @PathVariable String uuid,
                                         @RequestParam(required = false) Integer x, @RequestParam(required = false) Integer y) {
        // TODO Retrieve current local authority
        LocalAuthority currentLocalAuthority = localAuthorityService.getByName("SICTIAM-Test").get();
        Acte acte = acteService.getByUuid(uuid);
        byte[] pdf = new byte[0];
        if(!acteService.isActeACK(uuid)) {
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
        outputFile(response, pdf, acte.getActeAttachment().getFilename());
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/{uuid}/history/{historyUuid}/file")
    public ResponseEntity getFileHistory(HttpServletResponse response, @PathVariable String historyUuid) {
        ActeHistory acteHistory = acteService.getHistoryByUuid(historyUuid);
        if(acteHistory.getFile() != null) {
            outputFile(response, acteHistory.getFile(), acteHistory.getFileName());
            return new ResponseEntity(HttpStatus.OK);
        }
        else throw new FileNotFoundException();
    }

    @GetMapping("/{uuid}/annexes")
    public ResponseEntity<List<Attachment>> getAnnexes(@PathVariable String uuid) {
        List<Attachment> attachments = acteService.getAnnexes(uuid);
        return new ResponseEntity<>(attachments, HttpStatus.OK);
    }

    @GetMapping("/{uuid}/annexe/{annexeUuid}")
    public ResponseEntity getAnnexe(HttpServletResponse response, @PathVariable String annexeUuid) {
        Attachment annexe = acteService.getAnnexeByUuid(annexeUuid);
        outputFile(response, annexe.getFile(), annexe.getFilename());
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/{uuid}/status/cancel")
    public void cancel(@PathVariable String uuid) {
        acteService.cancel(uuid);
    }

    @PostMapping
    ResponseEntity<Object> create(@RequestParam("acte") String acteJson, @RequestParam("file") MultipartFile file,
                                  @RequestParam("annexes") MultipartFile... annexes) {
        // TODO Retrieve current local authority
        LocalAuthority currentLocalAuthority = localAuthorityService.getByName("SICTIAM-Test").get();
        ObjectMapper mapper = new ObjectMapper();
        try {
            Acte acte = mapper.readValue(acteJson, Acte.class);

            LOGGER.debug("Received acte : {}", acte.getObjet());
            LOGGER.debug("Received main file {} with {} annexes", file.getOriginalFilename(), annexes.length);
    		List<ObjectError> errors = ValidationUtil.validateActeWithFile(acte,file,annexes);
    		if(!errors.isEmpty()) {
				CustomValidationUI customValidationUI=new CustomValidationUI(errors, "has failed");
				return new ResponseEntity<>(customValidationUI, HttpStatus.BAD_REQUEST);
			}else {
    			 Acte result = acteService.create(currentLocalAuthority, acte, file, annexes);
    	         return new ResponseEntity<>(result.getUuid(), HttpStatus.CREATED);
    		}
           
        } catch (IOException e) {
            LOGGER.error("IOException: Could not convert JSON to Acte: {}", e);
            return new ResponseEntity<>("notifications.acte.sent.error.non_extractable_acte", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (ActeNotSentException ns){
            LOGGER.error("ActeNotSentException: {}", ns);
            return new ResponseEntity<>("notifications.acte.sent.error.acte_not_sent", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /* --------------------------- */
    /* ----- FILE OPERATIONS ----- */
    /* --------------------------- */

    private void outputCSV(HttpServletResponse response, Object[] beans, List<String> header, List<String> translatedHeader, String filename) {
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
            for (Object bean : beans) csvWriter.write(bean, arrayHeader);
            csvWriter.close();
        } catch (Exception e) {
            LOGGER.error("Error while trying to output CSV: {}", e);
        }
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
        String mimeType= URLConnection.guessContentTypeFromName(filename);
        if(mimeType==null){
            LOGGER.info("Mimetype is not detectable, will take default");
            mimeType = "application/octet-stream";
        }
        return mimeType;
    }
}

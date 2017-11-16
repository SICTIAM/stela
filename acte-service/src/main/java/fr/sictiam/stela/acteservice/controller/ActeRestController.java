package fr.sictiam.stela.acteservice.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.model.ActeHistory;
import fr.sictiam.stela.acteservice.model.ActeNature;
import fr.sictiam.stela.acteservice.model.Attachment;
import fr.sictiam.stela.acteservice.model.LocalAuthority;
import fr.sictiam.stela.acteservice.model.StatusType;
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
import javassist.tools.web.BadHttpRequest;

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
        boolean isCancellable = acteService.isCancellable(uuid);
        return new ResponseEntity<>(new ActeUI(acte, isCancellable), HttpStatus.OK);
    }

    @GetMapping("/{uuid}/AR_{uuid}.pdf")
    public void downloadACKPdf(HttpServletResponse response, @PathVariable String uuid, @RequestParam(required = false) String lng) {
        try {
            byte[] pdf = acteService.getACKPdfs(new ActeUuidsAndSearchUI(Collections.singletonList(uuid)), lng);
            outputFile(response, pdf, "AR_" + uuid + ".pdf");
        } catch (NoContentException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("Error while generating the ACK PDF: {}", e);
        }
    }

    // Hack: Not possible to have an infinite UUID list in a GET request with params
    @PostMapping("/ARs.pdf")
    public void downloadACKsPdf(HttpServletResponse response, @RequestBody ActeUuidsAndSearchUI acteUuidsAndSearchUI, @RequestParam(required = false) String lng) {
        try {
            byte[] pdf = acteService.getACKPdfs(acteUuidsAndSearchUI, lng);
            outputFile(response, pdf, "ARs.pdf");
        } catch (NoContentException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("Error while generating the ACKs PDF: {}", e);
        }
    }

    // Hack: Not possible to have an infinite UUID list in a GET request with params 
    @PostMapping("/actes.csv")
    public void getCSVFromList(HttpServletResponse response, @RequestBody ActeUuidsAndSearchUI acteUuidsAndSearchUI, @RequestParam(required = false) String lng) {
        List<String> fields = ActeCSVUI.getFields();
        List<String> translatedFields = acteService.getTranslatedCSVFields(fields, lng);
        outputCSV(response, acteService.getActesCSV(acteUuidsAndSearchUI, lng).toArray(), fields, translatedFields, "actes.csv");
    }

    @GetMapping("/{uuid}/file")
    public void getFile(HttpServletResponse response, @PathVariable String uuid) {
        Acte acte = acteService.getByUuid(uuid);
        outputFile(response, acte.getActeAttachment().getFile(), acte.getActeAttachment().getFilename());
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
				CustomValidationUI customValidationUI=new CustomValidationUI(errors, "as fail");
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

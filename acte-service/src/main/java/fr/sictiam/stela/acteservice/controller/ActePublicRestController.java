package fr.sictiam.stela.acteservice.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.lowagie.text.DocumentException;
import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.model.Attachment;
import fr.sictiam.stela.acteservice.model.ui.ActeUuidsAndSearchUI;
import fr.sictiam.stela.acteservice.model.ui.SearchResultsUI;
import fr.sictiam.stela.acteservice.model.ui.Views;
import fr.sictiam.stela.acteservice.service.ActeService;
import fr.sictiam.stela.acteservice.service.LocalAuthorityService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/acte/public")
public class ActePublicRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActePublicRestController.class);

    private final ActeService acteService;
    private final LocalAuthorityService localAuthorityService;

    public ActePublicRestController(ActeService acteService, LocalAuthorityService localAuthorityService) {
        this.acteService = acteService;
        this.localAuthorityService = localAuthorityService;
    }

    @GetMapping
    @JsonView(Views.ActePublicView.class)
    public ResponseEntity<SearchResultsUI> getAll(
            @RequestParam(value = "multifield", required = false) String multifield,
            @RequestParam(value = "number", required = false) String number,
            @RequestParam(value = "objet", required = false) String objet,
            @RequestParam(value = "siren", required = false) String siren,
            @RequestParam(value = "decisionFrom", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate decisionFrom,
            @RequestParam(value = "decisionTo", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate decisionTo,
            @RequestParam(value = "limit", required = false, defaultValue = "25") Integer limit,
            @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
            @RequestParam(value = "column", required = false, defaultValue = "creation") String column,
            @RequestParam(value = "direction", required = false, defaultValue = "DESC") String direction) {
        List<Acte> actes = acteService.getAllPublicWithQuery(multifield, number, objet, siren, decisionFrom, decisionTo,
                limit, offset, column, direction);
        Long count = acteService.countAllPublicWithQuery(multifield, number, objet, siren, decisionFrom, decisionTo);
        return new ResponseEntity<>(new SearchResultsUI(count, actes), HttpStatus.OK);
    }

    @GetMapping("/{uuid}")
    @JsonView(Views.ActePublicView.class)
    public ResponseEntity<Acte> getByUuid(@PathVariable String uuid) {
        Acte acte = acteService.getByUuid(uuid);
        return new ResponseEntity<>(acte, HttpStatus.OK);
    }

    @GetMapping("/{uuid}/AR_{uuid}.pdf")
    public ResponseEntity downloadACKPdf(
            @RequestParam(value = "disposition", required = false, defaultValue = "inline") String disposition,
            HttpServletResponse response, @PathVariable String uuid, @RequestParam(required = false) String lng) {
        try {
            byte[] pdf = acteService.getACKPdfs(new ActeUuidsAndSearchUI(Collections.singletonList(uuid)), lng);
            outputFile(response, pdf, "AR_" + uuid + ".pdf", disposition);
            return new ResponseEntity(HttpStatus.OK);
        } catch (IOException | DocumentException e) {
            LOGGER.error("Error while generating the ACK PDF: {}", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{uuid}/file")
    public ResponseEntity getActeAttachment(HttpServletResponse response, @PathVariable String uuid,
            @RequestParam(value = "disposition", required = false, defaultValue = "inline") String disposition) {
        Acte acte = acteService.getByUuid(uuid);
        outputFile(response, acte.getActeAttachment().getFile(), acte.getActeAttachment().getFilename(), disposition);
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/{uuid}/file/thumbnail")
    public ResponseEntity getActeAttachmentThumbnail(HttpServletResponse response, @PathVariable String uuid) {
        if (StringUtils.isNotBlank(uuid)) {
            try {
                byte[] thumbnail = acteService.getActeAttachmentThumbnail(uuid);
                outputFile(response, thumbnail, "thumbnail-" + uuid + ".png", "inline");
                return new ResponseEntity(HttpStatus.OK);
            } catch (IOException e) {
                LOGGER.error("Error trying to generate the PDF's thumbnail: {}", e);
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }
    }

    @GetMapping("/{uuid}/file/stamped")
    public ResponseEntity getStampedActeAttachment(
            @RequestParam(value = "disposition", required = false, defaultValue = "inline") String disposition,
            HttpServletResponse response, @PathVariable String uuid, @RequestParam(required = false) Integer x,
            @RequestParam(required = false) Integer y) {
        Acte acte = acteService.getByUuid(uuid);
        byte[] pdf = new byte[0];
        if (!acteService.isActeACK(uuid)) {
            pdf = acte.getActeAttachment().getFile();
        } else {
            try {
                pdf = acteService.getStampedActe(acte, x, y, acte.getLocalAuthority());
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

    @GetMapping("/{uuid}/annexe/{annexeUuid}")
    public ResponseEntity getAnnexe(HttpServletResponse response, @PathVariable String annexeUuid,
            @RequestParam(value = "disposition", required = false, defaultValue = "inline") String disposition) {
        Attachment annexe = acteService.getAnnexeByUuid(annexeUuid);
        outputFile(response, annexe.getFile(), annexe.getFilename(), disposition);
        return new ResponseEntity(HttpStatus.OK);
    }


    /* --------------------------- */
    /* ----- FILE OPERATIONS ----- */
    /* --------------------------- */

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

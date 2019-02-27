package fr.sictiam.stela.convocationservice.controller;

import com.fasterxml.jackson.annotation.JsonView;
import fr.sictiam.stela.convocationservice.model.Attachment;
import fr.sictiam.stela.convocationservice.model.LocalAuthority;
import fr.sictiam.stela.convocationservice.model.Recipient;
import fr.sictiam.stela.convocationservice.model.Right;
import fr.sictiam.stela.convocationservice.model.ui.Views;
import fr.sictiam.stela.convocationservice.model.util.RightUtils;
import fr.sictiam.stela.convocationservice.service.LocalAuthorityService;
import fr.sictiam.stela.convocationservice.service.RecipientService;
import fr.sictiam.stela.convocationservice.service.StorageService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/convocation/local-authority")
public class LocalAuthorityRestController {

    private final static Logger LOGGER = LoggerFactory.getLogger(LocalAuthorityRestController.class);

    private final LocalAuthorityService localAuthorityService;

    private final RecipientService recipientService;

    private final StorageService storageService;

    @Autowired
    public LocalAuthorityRestController(
            LocalAuthorityService localAuthorityService,
            RecipientService recipientService,
            StorageService storageService) {
        this.localAuthorityService = localAuthorityService;
        this.recipientService = recipientService;
        this.storageService = storageService;
    }


    @GetMapping("/recipients")
    @JsonView(Views.Public.class)
    public ResponseEntity<List<Recipient>> getRecipients(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Profile-UUID") String currentProfileUuid,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid) {

        if (!RightUtils.hasRight(rights, Arrays.asList(Right.values()))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<Recipient> recipients = recipientService.getAllByLocalAuthority(currentLocalAuthUuid);
        return new ResponseEntity<>(recipients, HttpStatus.OK);
    }

    @GetMapping
    @JsonView(Views.LocalAuthority.class)
    public ResponseEntity<LocalAuthority> getCurrent(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid) {

        if (!RightUtils.hasRight(rights, Arrays.asList(Right.values()))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(localAuthorityService.getByUuid(currentLocalAuthUuid), HttpStatus.OK);
    }

    @PutMapping
    @JsonView(Views.LocalAuthority.class)
    public ResponseEntity<LocalAuthority> updateCurrent(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestBody LocalAuthority params) {

        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.CONVOCATION_ADMIN))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        LocalAuthority localAuthority = localAuthorityService.update(currentLocalAuthUuid, params);
        return new ResponseEntity<>(localAuthority, HttpStatus.OK);
    }

    @GetMapping("/procuration")
    public ResponseEntity getProcuration(
            HttpServletResponse response,
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestParam(required = false, defaultValue = "inline") String disposition) {

        if (!RightUtils.hasRight(rights, Arrays.asList(Right.values()))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        LocalAuthority localAuthority = localAuthorityService.getLocalAuthority(currentLocalAuthUuid);

        Attachment procuration = localAuthority.getDefaultProcuration();
        if (procuration == null) {
            LOGGER.error("No procuration found for local authority", currentLocalAuthUuid);
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        byte[] content = storageService.getAttachmentContent(procuration);
        if (content == null) {
            LOGGER.error("Cannot retrieve file {} content", procuration.getUuid());
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        outputFile(response, content, procuration.getFilename(), disposition);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/procuration")
    public ResponseEntity uploadProcuration(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestParam MultipartFile procuration) {

        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.CONVOCATION_ADMIN))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        LocalAuthority localAuthority = localAuthorityService.getLocalAuthority(currentLocalAuthUuid);
        localAuthorityService.addProcuration(localAuthority, procuration);

        return new ResponseEntity(HttpStatus.OK);
    }

    private void outputFile(HttpServletResponse response, byte[] file, String filename, String disposition) {
        try {
            InputStream fileInputStream = new ByteArrayInputStream(file);

            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, String.format(disposition + "; filename=" + filename));
            response.addHeader(HttpHeaders.CONTENT_TYPE, getContentType(filename));

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

package fr.sictiam.stela.convocationservice.controller;

import com.fasterxml.jackson.annotation.JsonView;
import fr.sictiam.stela.convocationservice.model.Attachment;
import fr.sictiam.stela.convocationservice.model.Convocation;
import fr.sictiam.stela.convocationservice.model.Recipient;
import fr.sictiam.stela.convocationservice.model.Right;
import fr.sictiam.stela.convocationservice.model.StatusType;
import fr.sictiam.stela.convocationservice.model.exception.ConvocationException;
import fr.sictiam.stela.convocationservice.model.exception.NotFoundException;
import fr.sictiam.stela.convocationservice.model.ui.ReceivedConvocationDetailUI;
import fr.sictiam.stela.convocationservice.model.ui.ReceivedConvocationUI;
import fr.sictiam.stela.convocationservice.model.ui.SearchResultsUI;
import fr.sictiam.stela.convocationservice.model.ui.Views;
import fr.sictiam.stela.convocationservice.model.util.RightUtils;
import fr.sictiam.stela.convocationservice.service.ConvocationService;
import fr.sictiam.stela.convocationservice.service.RecipientService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/convocation")
public class ConvocationRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConvocationRestController.class);

    private final ConvocationService convocationService;

    private final RecipientService recipientService;

    @Autowired
    public ConvocationRestController(
            ConvocationService convocationService,
            RecipientService recipientService) {
        this.convocationService = convocationService;
        this.recipientService = recipientService;
    }


    @JsonView(Views.SearchSentConvocation.class)
    @GetMapping("/sent")
    public ResponseEntity<SearchResultsUI> getSentConvocation(
            @RequestParam(value = "multifield", required = false) String multifield,
            @RequestParam(value = "sentDateFrom", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate sentDateFrom,
            @RequestParam(value = "sentDateTo", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate sentDateTo,
            @RequestParam(value = "meetingDateFrom", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate meetingDateFrom,
            @RequestParam(value = "meetingDateTo", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate meetingDateTo,
            @RequestParam(value = "assemblyType", required = false) String assemblyType,
            @RequestParam(value = "subject", required = false) String subject,
            @RequestParam(value = "limit", required = false, defaultValue = "25") Integer limit,
            @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
            @RequestParam(value = "column", required = false, defaultValue = "meetingDate") String column,
            @RequestParam(value = "direction", required = false, defaultValue = "DESC") String direction,
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute("STELA-Current-Profile-UUID") String currentProfileUuid) {

        if (!RightUtils.hasRight(rights, Arrays.asList(Right.CONVOCATION_DEPOSIT, Right.CONVOCATION_ADMIN))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<Convocation> convocations = convocationService.findSentWithQuery(multifield, sentDateFrom, sentDateTo,
                assemblyType, meetingDateFrom, meetingDateTo, subject, limit, offset, column, direction, currentLocalAuthUuid);

        Long count = convocationService.countSentWithQuery(multifield, sentDateFrom, sentDateTo, assemblyType,
                meetingDateFrom, meetingDateTo, subject, currentLocalAuthUuid);

        return new ResponseEntity<>(new SearchResultsUI(count, convocations), HttpStatus.OK);
    }


    @GetMapping("/received")
    public ResponseEntity<SearchResultsUI> getReceivedConvocation(
            @RequestParam(value = "multifield", required = false) String multifield,
            @RequestParam(value = "meetingDateFrom", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate meetingDateFrom,
            @RequestParam(value = "meetingDateTo", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate meetingDateTo,
            @RequestParam(value = "assemblyType", required = false) String assemblyType,
            @RequestParam(value = "subject", required = false) String subject,
            @RequestParam(value = "filter", required = false) String filter,
            @RequestParam(value = "limit", required = false, defaultValue = "25") Integer limit,
            @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
            @RequestParam(value = "column", required = false, defaultValue = "meetingDate") String column,
            @RequestParam(value = "direction", required = false, defaultValue = "DESC") String direction,
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute(name = "STELA-Current-Profile-UUID", required = false) String currentProfileUuid,
            @RequestAttribute(name = "STELA-Current-Recipient", required = false) Recipient recipient) {

        if (StringUtils.isNotEmpty(currentProfileUuid) && recipient == null) {
            try {
                recipient = recipientService.findByProfileinLocalAuthority(currentProfileUuid, currentLocalAuthUuid);
            } catch (NotFoundException e) {
                LOGGER.error(e.getMessage());
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        }

        if (!RightUtils.hasRight(rights, Arrays.asList(Right.values()))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        final Recipient currentRecipient = recipient;
        List<ReceivedConvocationUI> convocations = convocationService.findReceivedWithQuery(multifield, assemblyType,
                meetingDateFrom, meetingDateTo, subject, filter, limit, offset, column, direction, currentLocalAuthUuid,
                recipient).stream().map(convocation -> new ReceivedConvocationUI(convocation, currentRecipient)).collect(Collectors.toList());


        Long count = convocationService.countReceivedWithQuery(multifield, assemblyType,
                meetingDateFrom, meetingDateTo, subject, filter, currentLocalAuthUuid, recipient);

        return new ResponseEntity<>(new SearchResultsUI(count, convocations), HttpStatus.OK);
    }


    @JsonView(Views.ConvocationInternal.class)
    @GetMapping("/{uuid}")
    public ResponseEntity<Convocation> getConvocation(
            @PathVariable String uuid,
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute("STELA-Current-Profile-UUID") String currentProfileUuid) {

        if (!RightUtils.hasRight(rights, Arrays.asList(Right.values())) || !hasAccess(uuid, currentProfileUuid, null)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Convocation convocation = convocationService.getConvocation(uuid, currentLocalAuthUuid);
        return new ResponseEntity<>(convocation, HttpStatus.OK);
    }

    @GetMapping("/received/{uuid}")
    public ResponseEntity<ReceivedConvocationUI> openConvocation(
            @PathVariable String uuid,
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute(name = "STELA-Current-Profile-UUID", required = false) String profileUuid,
            @RequestAttribute(name = "STELA-Current-Recipient", required = false) Recipient recipient) {

        if (StringUtils.isNotEmpty(profileUuid) && recipient == null) {
            try {
                recipient = recipientService.findByProfileinLocalAuthority(profileUuid, currentLocalAuthUuid);
            } catch (NotFoundException e) {
                LOGGER.error(e.getMessage());
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        }

        if (!RightUtils.hasRight(rights, Arrays.asList(Right.values()))
                || !hasAccess(uuid, null, recipient)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Convocation convocation = convocationService.getConvocation(uuid, currentLocalAuthUuid);
        convocationService.openBy(convocation, recipient);
        return new ResponseEntity<>(new ReceivedConvocationDetailUI(convocation, recipient), HttpStatus.OK);
    }

    @JsonView(Views.ConvocationInternal.class)
    @PostMapping
    public ResponseEntity<?> create(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute("STELA-Current-Profile-UUID") String currentProfileUuid,
            @Valid @RequestBody Convocation params) {

        if (!RightUtils.hasRight(rights, Arrays.asList(Right.CONVOCATION_DEPOSIT, Right.CONVOCATION_ADMIN))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            Convocation convocation = convocationService.create(params, currentLocalAuthUuid, currentProfileUuid);
            return new ResponseEntity<>(convocation, HttpStatus.CREATED);
        } catch (ConvocationException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @JsonView(Views.Convocation.class)
    @PutMapping("/{uuid}")
    public ResponseEntity<Convocation> update(
            @PathVariable String uuid,
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestBody Convocation params) {

        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.CONVOCATION_DISPLAY))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Convocation convocation = convocationService.update(uuid, currentLocalAuthUuid, params);
        return new ResponseEntity<>(convocation, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<SearchResultsUI> getAll(@RequestParam(value = "objet", required = false) String objet,
            @RequestParam(value = "creationFrom", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate
                    creationFrom,
            @RequestParam(value = "creationTo", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate
                    creationTo,
            @RequestParam(value = "status", required = false) StatusType status,
            @RequestParam(value = "limit", required = false, defaultValue = "25") Integer limit,
            @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
            @RequestParam(value = "column", required = false, defaultValue = "creation") String column,
            @RequestParam(value = "direction", required = false, defaultValue = "DESC") String direction,
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid) {

        if (!RightUtils.hasRight(rights, Arrays.asList(Right.values()))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(new SearchResultsUI(), HttpStatus.OK);
    }

    @PostMapping("/{uuid}/upload")
    public ResponseEntity<?> uploadFiles(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute("STELA-Current-Profile-UUID") String currentProfileUuid,
            @PathVariable String uuid,
            @RequestParam(name = "file", required = false) MultipartFile file,
            @RequestParam(name = "annexes", required = false) MultipartFile... annexes) {

        if (!RightUtils.hasRight(rights, Arrays.asList(Right.CONVOCATION_DEPOSIT, Right.CONVOCATION_ADMIN))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Convocation convocation = convocationService.getConvocation(uuid);

        convocationService.uploadFiles(convocation, file, annexes);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/{uuid}/file/{fileUuid}")
    public ResponseEntity getFile(
            HttpServletResponse response,
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute(name = "STELA-Current-Profile-UUID", required = false) String profileUuid,
            @RequestAttribute(name = "STELA-Current-Recipient", required = false) Recipient recipient,
            @PathVariable String uuid,
            @PathVariable String fileUuid,
            @RequestParam(value = "disposition", required = false, defaultValue = "inline") String disposition) {

        if (!RightUtils.hasRight(rights, Arrays.asList(Right.values()))
                || !hasAccess(uuid, profileUuid, recipient)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Attachment file = convocationService.getFile(currentLocalAuthUuid, uuid, fileUuid);
        outputFile(response, file.getContent(), file.getFilename(), disposition);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    private String getContentType(String filename) {
        String mimeType = URLConnection.guessContentTypeFromName(filename);
        if (mimeType == null) {
            LOGGER.info("Mimetype is not detectable, will take default");
            mimeType = "application/octet-stream";
        }
        return mimeType;
    }

    private boolean hasAccess(String convocationUuid, String profileUuid, Recipient recipient) {

        try {
            Convocation convocation = convocationService.getConvocation(convocationUuid);
            return (StringUtils.isNotEmpty(profileUuid) && convocation.getProfileUuid().equals(profileUuid))
                    || (recipient != null && convocation.getRecipientResponses().stream().anyMatch(r -> r.getRecipient().equals(recipient)));
        } catch (NotFoundException e) {
            LOGGER.error("Access to convocation not granted: {}", e.getMessage());
            return false;
        }
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
}

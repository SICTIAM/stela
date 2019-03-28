package fr.sictiam.stela.convocationservice.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.itextpdf.text.DocumentException;
import fr.sictiam.stela.convocationservice.model.Attachment;
import fr.sictiam.stela.convocationservice.model.Convocation;
import fr.sictiam.stela.convocationservice.model.Recipient;
import fr.sictiam.stela.convocationservice.model.ResponseType;
import fr.sictiam.stela.convocationservice.model.Right;
import fr.sictiam.stela.convocationservice.model.exception.AccessNotGrantedException;
import fr.sictiam.stela.convocationservice.model.exception.ConvocationCancelledException;
import fr.sictiam.stela.convocationservice.model.exception.ConvocationException;
import fr.sictiam.stela.convocationservice.model.exception.ConvocationNotAvailableException;
import fr.sictiam.stela.convocationservice.model.exception.NotFoundException;
import fr.sictiam.stela.convocationservice.model.ui.ReceivedConvocationDetailUI;
import fr.sictiam.stela.convocationservice.model.ui.ReceivedConvocationUI;
import fr.sictiam.stela.convocationservice.model.ui.SearchResultsUI;
import fr.sictiam.stela.convocationservice.model.ui.Views;
import fr.sictiam.stela.convocationservice.model.util.RightUtils;
import fr.sictiam.stela.convocationservice.service.ConvocationService;
import fr.sictiam.stela.convocationservice.service.RecipientService;
import fr.sictiam.stela.convocationservice.service.util.DocumentGenerator;
import fr.sictiam.stela.convocationservice.service.util.DocumentGeneratorFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

    private final String forbiddenCharactersInFilename = "[\\\"/<>:\\?\\*|]";

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
            @RequestParam(value = "filter", required = false) String filter,
            @RequestParam(value = "limit", required = false, defaultValue = "25") Integer limit,
            @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
            @RequestParam(value = "column", required = false, defaultValue = "meetingDate") String column,
            @RequestParam(value = "direction", required = false, defaultValue = "DESC") String direction,
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute("STELA-Current-Profile-UUID") String currentProfileUuid,
            @RequestAttribute("STELA-Current-Profile-Groups") Set<String> groups) {

        validateAccess(currentLocalAuthUuid, null, currentProfileUuid, null,
                rights, Arrays.asList(Right.CONVOCATION_DEPOSIT, Right.CONVOCATION_ADMIN), false);

        List<Convocation> convocations = convocationService.findSentWithQuery(multifield, sentDateFrom, sentDateTo,
                assemblyType, meetingDateFrom, meetingDateTo, subject, filter, limit, offset, column, direction,
                currentLocalAuthUuid, groups);

        Long count = convocationService.countSentWithQuery(multifield, sentDateFrom, sentDateTo, assemblyType,
                meetingDateFrom, meetingDateTo, subject, filter, currentLocalAuthUuid, groups);

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

        final Recipient currentRecipient = validateAccess(currentLocalAuthUuid, null, currentProfileUuid, recipient, rights, Arrays.asList(Right.values()),
                true);

        List<ReceivedConvocationUI> convocations = convocationService.findReceivedWithQuery(multifield, assemblyType,
                meetingDateFrom, meetingDateTo, subject, filter, limit, offset, column, direction, currentLocalAuthUuid,
                currentRecipient).stream().map(convocation -> new ReceivedConvocationUI(convocation, currentRecipient)).collect(Collectors.toList());


        Long count = convocationService.countReceivedWithQuery(multifield, assemblyType,
                meetingDateFrom, meetingDateTo, subject, filter, currentLocalAuthUuid, currentRecipient);

        return new ResponseEntity<>(new SearchResultsUI(count, convocations), HttpStatus.OK);
    }


    @JsonView(Views.ConvocationInternal.class)
    @GetMapping("/{uuid}")
    public ResponseEntity<Convocation> getConvocation(
            @PathVariable String uuid,
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute("STELA-Current-Profile-UUID") String currentProfileUuid) {

        validateAccess(currentLocalAuthUuid, uuid, currentProfileUuid, null, rights, Arrays.asList(Right.values()),
                false);

        Convocation convocation = convocationService.getConvocation(uuid, currentLocalAuthUuid);
        convocation.setProfile(convocationService.retrieveProfile(convocation.getProfileUuid()));
        return new ResponseEntity<>(convocation, HttpStatus.OK);
    }

    @GetMapping("/received/{uuid}")
    public ResponseEntity<ReceivedConvocationUI> openConvocation(
            @PathVariable String uuid,
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute(name = "STELA-Current-Profile-UUID", required = false) String profileUuid,
            @RequestAttribute(name = "STELA-Current-Recipient", required = false) Recipient recipient) {

        final Recipient currentRecipient = validateAccess(currentLocalAuthUuid, uuid, profileUuid, recipient, rights,
                Arrays.asList(Right.values()), true);

        Convocation convocation = convocationService.getConvocation(uuid, currentLocalAuthUuid);
        convocationService.openBy(convocation, currentRecipient);
        return new ResponseEntity<>(new ReceivedConvocationDetailUI(convocation, currentRecipient), HttpStatus.OK);
    }

    @JsonView(Views.ConvocationInternal.class)
    @PostMapping
    public ResponseEntity<?> create(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute("STELA-Current-Profile-UUID") String currentProfileUuid,
            @Valid @RequestBody Convocation params) {

        validateAccess(currentLocalAuthUuid, null, currentProfileUuid, null, rights,
                Collections.singletonList(Right.CONVOCATION_DEPOSIT), false);

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
            @RequestAttribute("STELA-Current-Profile-UUID") String currentProfileUuid,
            @RequestBody Convocation params) {

        validateAccess(currentLocalAuthUuid, uuid, currentProfileUuid, null, rights,
                Collections.singletonList(Right.CONVOCATION_DEPOSIT), false);

        Convocation convocation = convocationService.update(uuid, currentLocalAuthUuid, params);
        return new ResponseEntity<>(convocation, HttpStatus.OK);
    }

    @PostMapping("/{uuid}/upload")
    public ResponseEntity<?> uploadFiles(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute("STELA-Current-Profile-UUID") String currentProfileUuid,
            @PathVariable String uuid,
            @RequestParam(name = "file") MultipartFile file,
            @RequestParam(name = "procuration", required = false) MultipartFile procuration,
            @RequestParam(name = "annexes", required = false) MultipartFile[] annexes,
            @RequestParam(name = "tags", required = false) String[] tags) {

        validateAccess(currentLocalAuthUuid, uuid, currentProfileUuid, null, rights,
                Collections.singletonList(Right.CONVOCATION_DEPOSIT), false);

        Convocation convocation = convocationService.getConvocation(uuid);

        convocationService.uploadFiles(convocation, file, procuration, annexes, tags);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/{uuid}/upload")
    public ResponseEntity<?> uploadAdditionalFiles(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute("STELA-Current-Profile-UUID") String currentProfileUuid,
            @PathVariable String uuid,
            @RequestParam(name = "annexes", required = false) MultipartFile[] annexes,
            @RequestParam(name = "tags", required = false) String[] tags) {

        validateAccess(currentLocalAuthUuid, uuid, currentProfileUuid, null, rights,
                Collections.singletonList(Right.CONVOCATION_DEPOSIT), false);

        Convocation convocation = convocationService.getConvocation(uuid);

        convocationService.uploadAdditionalFiles(convocation, annexes, tags);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/{uuid}/upload-minutes")
    public ResponseEntity<?> uploadMinutes(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute("STELA-Current-Profile-UUID") String currentProfileUuid,
            @PathVariable String uuid,
            @RequestParam MultipartFile minutes) {

        validateAccess(currentLocalAuthUuid, uuid, currentProfileUuid, null, rights,
                Collections.singletonList(Right.CONVOCATION_DEPOSIT), false);

        Convocation convocation = convocationService.getConvocation(uuid);

        convocationService.uploadMinutes(convocation, minutes);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/{uuid}/file/{fileUuid}")
    public ResponseEntity getFile(
            HttpServletResponse response,
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute(name = "STELA-Current-Profile-UUID", required = false) String currentProfileUuid,
            @RequestAttribute(name = "STELA-Current-Recipient", required = false) Recipient recipient,
            @PathVariable String uuid,
            @PathVariable String fileUuid,
            @RequestParam(required = false) Boolean stamped,
            @RequestParam(required = false) Integer x,
            @RequestParam(required = false) Integer y,
            @RequestParam(required = false, defaultValue = "inline") String disposition) {

        validateAccess(currentLocalAuthUuid, uuid, currentProfileUuid, null, rights,
                Arrays.asList(Right.values()), false);

        Convocation convocation = convocationService.getConvocation(uuid, currentLocalAuthUuid);
        Attachment file = convocationService.getFile(convocation, fileUuid);

        try {
            byte[] content = file.getContent();
            if (content == null) {
                LOGGER.error("Cannot retrieve file {} content", file.getUuid());
                return new ResponseEntity(HttpStatus.NOT_FOUND);
            }

            if (convocation.getSentDate() != null && stamped != null && stamped == Boolean.TRUE
                    && getContentType(file.getFilename()).equals(MediaType.APPLICATION_PDF_VALUE)) {
                content = convocationService.getStampedFile(content, convocation.getSentDate(),
                        convocation.getLocalAuthority(), x, y);
            }
            outputFile(response, content, file.getFilename(), disposition);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IOException | DocumentException e) {
            LOGGER.error("Error during getting file {} for convocation {}: {}", fileUuid, uuid, e.getMessage());
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/{uuid}/archive")
    public ResponseEntity getArchive(
            HttpServletResponse response,
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute(name = "STELA-Current-Profile-UUID", required = false) String currentProfileUuid,
            @PathVariable String uuid,
            @RequestParam(required = false, defaultValue = "inline") String disposition) {

        validateAccess(currentLocalAuthUuid, uuid, currentProfileUuid, null, rights,
                Arrays.asList(Right.values()), false);

        Convocation convocation = convocationService.getConvocation(uuid, currentLocalAuthUuid);


        try {
            ByteArrayOutputStream baos = convocationService.createArchive(convocation);

            outputFile(response, baos.toByteArray(),
                    convocation.getSubject().replaceAll(forbiddenCharactersInFilename, "_") + ".tar.gz", disposition);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IOException e) {
            LOGGER.error("Error during generating archive for convocation {}: {}", uuid, e.getMessage());
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{uuid}/presence.{extension}")
    public ResponseEntity getPresenceList(
            HttpServletResponse response,
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @PathVariable String uuid,
            @PathVariable DocumentGeneratorFactory.Extension extension) {


        DocumentGenerator document = DocumentGeneratorFactory.of(extension);
        Convocation convocation = convocationService.getConvocation(uuid, currentLocalAuthUuid);

        byte[] content = document.generatePresenceList(convocation);
        outputFile(response, content,
                convocation.getSubject().replaceAll(forbiddenCharactersInFilename, "_") + "." + extension.name(),
                "inline");

        return new ResponseEntity(HttpStatus.OK);
    }

    @PutMapping("/received/{uuid}/{responseType}")
    public ResponseEntity answerConvocation(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute(name = "STELA-Current-Profile-UUID", required = false) String currentProfileUuid,
            @RequestAttribute(name = "STELA-Current-Recipient", required = false) Recipient recipient,
            @PathVariable String uuid,
            @PathVariable ResponseType responseType) {

        return handleConvocationResponse(rights, currentLocalAuthUuid, currentProfileUuid, recipient, uuid,
                responseType, null);
    }

    @PutMapping("/received/{uuid}/{responseType}/{substituteUuid}")
    public ResponseEntity answerConvocationWithProcuration(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute(name = "STELA-Current-Profile-UUID", required = false) String currentProfileUuid,
            @RequestAttribute(name = "STELA-Current-Recipient", required = false) Recipient recipient,
            @PathVariable String uuid,
            @PathVariable ResponseType responseType,
            @PathVariable String substituteUuid) {

        return handleConvocationResponse(rights, currentLocalAuthUuid, currentProfileUuid, recipient, uuid,
                responseType, substituteUuid);
    }

    @PutMapping("/received/{uuid}/question/{questionUuid}/{value}")
    public ResponseEntity answerQuestion(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute(name = "STELA-Current-Profile-UUID", required = false) String currentProfileUuid,
            @RequestAttribute(name = "STELA-Current-Recipient", required = false) Recipient recipient,
            @PathVariable String uuid,
            @PathVariable String questionUuid,
            @PathVariable Boolean value) {

        final Recipient currentRecipient = validateAccess(currentLocalAuthUuid, uuid, currentProfileUuid, recipient,
                rights, Arrays.asList(Right.values()), true);

        Convocation convocation = convocationService.getConvocation(uuid, currentLocalAuthUuid);
        convocationService.answerQuestion(convocation, currentRecipient, questionUuid, value);

        return new ResponseEntity(HttpStatus.OK);
    }

    @PutMapping("/{uuid}/cancel")
    public ResponseEntity cancelConvocation(
            @PathVariable String uuid,
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute("STELA-Current-Profile-UUID") String currentProfileUuid) {

        validateAccess(currentLocalAuthUuid, uuid, currentProfileUuid, null, rights,
                Collections.singletonList(Right.CONVOCATION_DEPOSIT), false);

        Convocation convocation = convocationService.getConvocation(uuid, currentLocalAuthUuid);
        convocationService.cancelConvocation(convocation);

        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/{uuid}/file/{fileUuid}/tag/{tagUuid}")
    public ResponseEntity addTag(
            @PathVariable String uuid,
            @PathVariable String fileUuid,
            @PathVariable String tagUuid,
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute("STELA-Current-Profile-UUID") String currentProfileUuid) {

        validateAccess(currentLocalAuthUuid, uuid, currentProfileUuid, null, rights,
                Collections.singletonList(Right.CONVOCATION_DEPOSIT), false);

        Convocation convocation = convocationService.getConvocation(uuid, currentLocalAuthUuid);
        convocationService.addTagToFile(convocation, fileUuid, tagUuid);
        return new ResponseEntity(HttpStatus.OK);
    }

    @DeleteMapping("/{uuid}/file/{fileUuid}/tag/{tagUuid}")
    public ResponseEntity removeTag(
            @PathVariable String uuid,
            @PathVariable String fileUuid,
            @PathVariable String tagUuid,
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute("STELA-Current-Profile-UUID") String currentProfileUuid) {

        validateAccess(currentLocalAuthUuid, uuid, currentProfileUuid, null, rights,
                Collections.singletonList(Right.CONVOCATION_DEPOSIT), false);

        Convocation convocation = convocationService.getConvocation(uuid, currentLocalAuthUuid);
        convocationService.removeTagFromFile(convocation, fileUuid, tagUuid);
        return new ResponseEntity(HttpStatus.OK);
    }

    private String getContentType(String filename) {
        String mimeType = URLConnection.guessContentTypeFromName(filename);
        if (mimeType == null) {
            LOGGER.info("Mimetype is not detectable, will take default");
            mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        return mimeType;
    }

    private boolean isRecipient(String convocationUuid, String profileUuid, Recipient recipient) {

        try {
            Convocation convocation = convocationService.getConvocation(convocationUuid);
            return recipient != null && convocation.getRecipientResponses()
                    .stream().anyMatch(r -> r.getRecipient().equals(recipient));
        } catch (NotFoundException e) {
            LOGGER.error("Access to convocation not granted: {}", e.getMessage());
            return false;
        }
    }

    private Recipient validateAccess(String localAuthorityUuid, String convocationUuid, String profileUuid,
            Recipient recipient, Set<Right> rights, List<Right> authorizedRights, boolean recipientOnly)
            throws AccessNotGrantedException {

        if (StringUtils.isNotEmpty(profileUuid) && recipient == null) {
            try {
                recipient = recipientService.findByProfileinLocalAuthority(profileUuid, localAuthorityUuid);
            } catch (NotFoundException e) {
                LOGGER.error(e.getMessage());
            }
        }

        if (!RightUtils.hasRight(rights, authorizedRights)
                || (recipientOnly && convocationUuid != null && !isRecipient(convocationUuid, profileUuid, recipient))) {
            throw new AccessNotGrantedException();
        }

        return recipient;
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

    private ResponseEntity handleConvocationResponse(Set<Right> rights, String currentLocalAuthUuid, String currentProfileUuid,
            Recipient recipient, String uuid, ResponseType responseType, String substituteUuid) {

        final Recipient currentRecipient = validateAccess(currentLocalAuthUuid, uuid, currentProfileUuid, recipient,
                rights, Arrays.asList(Right.values()), true);

        Convocation convocation = convocationService.getConvocation(uuid, currentLocalAuthUuid);
        convocationService.answerConvocation(convocation, currentRecipient, responseType, substituteUuid);
        return new ResponseEntity(HttpStatus.OK);
    }

    @ExceptionHandler(AccessNotGrantedException.class)
    public ResponseEntity accessNotGrantedHandler(HttpServletRequest request, AccessNotGrantedException exception) {
        return new ResponseEntity(HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity notFoundHandler(HttpServletRequest request, NotFoundException exception) {
        return new ResponseEntity(HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ConvocationCancelledException.class)
    public ResponseEntity<String> convocationCancelledHandler(HttpServletRequest request,
            ConvocationCancelledException exception) {
        return new ResponseEntity<>("convocation.errors.convocation.alreadyCancelled", HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ConvocationNotAvailableException.class)
    public ResponseEntity<String> convocationNotAvailableHandler(HttpServletRequest request,
            ConvocationNotAvailableException exception) {
        return new ResponseEntity<>(String.format("convocation.errors.convocation.%s", exception.getMessage()),
                HttpStatus.CONFLICT);
    }
}

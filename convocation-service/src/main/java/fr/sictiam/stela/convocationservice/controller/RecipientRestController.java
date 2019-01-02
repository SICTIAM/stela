package fr.sictiam.stela.convocationservice.controller;

import com.fasterxml.jackson.annotation.JsonView;
import fr.sictiam.stela.convocationservice.dao.AssemblyTypeRepository;
import fr.sictiam.stela.convocationservice.model.Recipient;
import fr.sictiam.stela.convocationservice.model.Right;
import fr.sictiam.stela.convocationservice.model.ui.SearchResultsUI;
import fr.sictiam.stela.convocationservice.model.ui.Views;
import fr.sictiam.stela.convocationservice.model.util.RightUtils;
import fr.sictiam.stela.convocationservice.service.LocalAuthorityService;
import fr.sictiam.stela.convocationservice.service.NotificationService;
import fr.sictiam.stela.convocationservice.service.RecipientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/convocation/recipient")
public class RecipientRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecipientRestController.class);

    @Autowired
    AssemblyTypeRepository assemblyTypeRepository;

    @Autowired
    RecipientService recipientService;

    @Autowired
    LocalAuthorityService localAuthorityService;

    @Autowired
    NotificationService notificationService;


    @Autowired
    public RecipientRestController() {
    }

    @JsonView(Views.SearchRecipient.class)
    @GetMapping
    public ResponseEntity<SearchResultsUI> getAll(
            @RequestParam(value = "multifield", required = false) String multifield,
            @RequestParam(value = "firstname", required = false) String firstname,
            @RequestParam(value = "lastname", required = false) String lastname,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "limit", required = false, defaultValue = "25") Integer limit,
            @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
            @RequestParam(value = "column", required = false, defaultValue = "lastname") String column,
            @RequestParam(value = "direction", required = false, defaultValue = "ASC") String direction,
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid) {

        if (!RightUtils.hasRight(rights, Arrays.asList(Right.values()))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<Recipient> recipients = recipientService.findAllWithQuery(multifield, firstname, lastname, email, active,
                limit, offset, column, direction, currentLocalAuthUuid);

        Long count = recipientService.countAllWithQuery(multifield, firstname, lastname, email, active,
                limit, offset, column, direction, currentLocalAuthUuid);

        return new ResponseEntity<>(new SearchResultsUI(count, recipients), HttpStatus.OK);
    }


    @JsonView(Views.RecipientInternal.class)
    @GetMapping("/{uuid}")
    public ResponseEntity<Recipient> get(
            @PathVariable String uuid,
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid) {

        if (!RightUtils.hasRight(rights, Arrays.asList(Right.values()))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(recipientService.getRecipient(uuid, currentLocalAuthUuid), HttpStatus.OK);
    }


    @JsonView(Views.RecipientInternal.class)
    @PostMapping
    public ResponseEntity<Recipient> create(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Profile-UUID") String currentProfileUuid,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestBody Recipient recipient) {

        if (!RightUtils.hasRight(rights, Arrays.asList(Right.CONVOCATION_ADMIN))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        recipient = recipientService.create(recipient, currentLocalAuthUuid);
        return new ResponseEntity<>(recipient, HttpStatus.OK);
    }

    @JsonView(Views.RecipientInternal.class)
    @PutMapping("/{uuid}")
    public ResponseEntity<Recipient> update(
            @PathVariable String uuid,
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Profile-UUID") String currentProfileUuid,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestBody Recipient recipientParams) {

        if (!RightUtils.hasRight(rights, Arrays.asList(Right.CONVOCATION_ADMIN))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Recipient recipient = recipientService.update(uuid, currentLocalAuthUuid, recipientParams);

        return new ResponseEntity<>(recipient, HttpStatus.OK);
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

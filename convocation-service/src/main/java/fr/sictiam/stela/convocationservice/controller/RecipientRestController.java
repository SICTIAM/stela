package fr.sictiam.stela.convocationservice.controller;

import com.fasterxml.jackson.annotation.JsonView;
import fr.sictiam.stela.convocationservice.dao.AssemblyTypeRepository;
import fr.sictiam.stela.convocationservice.model.Recipient;
import fr.sictiam.stela.convocationservice.model.Right;
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

    @JsonView(Views.UserLocalAuthorityView.class)
    @GetMapping
    public ResponseEntity<List<Recipient>> getAll(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid) {

        if (!RightUtils.hasRight(rights, Arrays.asList(Right.values()))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<Recipient> recipients = recipientService.findAll(currentLocalAuthUuid);
        return new ResponseEntity<>(recipients, HttpStatus.OK);
    }


    @JsonView(Views.UserViewPublic.class)
    @GetMapping("/{uuid}")
    public ResponseEntity<Recipient> getAssemblyType(
            @PathVariable String uuid,
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Profile-UUID") String currentProfileUuid,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid) {

        if (!RightUtils.hasRight(rights, Arrays.asList(Right.values()))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(recipientService.getRecipient(uuid), HttpStatus.OK);
    }


    @JsonView(Views.UserViewPublic.class)
    @PostMapping("/new")
    public ResponseEntity<Recipient> create(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Profile-UUID") String currentProfileUuid,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestParam("firstname") String firstname,
            @RequestParam("lastname") String lastname,
            @RequestParam("email") String email,
            @RequestParam("phoneNumber") String phoneNumber) {

        if (!RightUtils.hasRight(rights, Arrays.asList(Right.CONVOCATION_ADMIN))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        firstname = firstname.trim();
        lastname = lastname.trim();
        email = email.trim();
        phoneNumber = phoneNumber.trim();

        Recipient recipient = recipientService.createFrom(firstname, lastname, email, phoneNumber,
                currentLocalAuthUuid);
        recipient = recipientService.save(recipient);
        return new ResponseEntity<>(recipient, HttpStatus.OK);
    }

    @PutMapping("/{uuid}/active")
    public ResponseEntity<?> setActive(
            @PathVariable String uuid,
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Profile-UUID") String currentProfileUuid,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid) {

        if (!RightUtils.hasRight(rights, Arrays.asList(Right.values()))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        recipientService.setActive(uuid, true);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/{uuid}/inactive")
    public ResponseEntity<?> setInactive(
            @PathVariable String uuid,
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Profile-UUID") String currentProfileUuid,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid) {

        if (!RightUtils.hasRight(rights, Arrays.asList(Right.values()))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        recipientService.setActive(uuid, false);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @GetMapping("/mail")
    public ResponseEntity<?> sendMail() {

        try {
            notificationService.sendMail("gerald.gole@gmail.om", "Test", "Body");
        } catch (Exception e) {
            LOGGER.error("Error while sending mail : ({}) : {]", e.getClass(), e.getMessage());
        }
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
}

package fr.sictiam.stela.convocationservice.controller;

import com.fasterxml.jackson.annotation.JsonView;
import fr.sictiam.stela.convocationservice.dao.AssemblyTypeRepository;
import fr.sictiam.stela.convocationservice.model.Recipient;
import fr.sictiam.stela.convocationservice.model.Right;
import fr.sictiam.stela.convocationservice.model.ui.Views;
import fr.sictiam.stela.convocationservice.model.util.RightUtils;
import fr.sictiam.stela.convocationservice.service.LocalAuthorityService;
import fr.sictiam.stela.convocationservice.service.RecipientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLConnection;
import java.util.Arrays;
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
    public RecipientRestController() {
    }

    /*
        @GetMapping
        public ResponseEntity<List<AssemblyType.Light>> getAll(
                @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
                @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid) {

            if (!RightUtils.hasRight(rights, Arrays.asList(Right.values()))) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            List<AssemblyType.Light> assemblyTypes = assemblyTypeService.findAllSimple(currentLocalAuthUuid);
            return new ResponseEntity<>(assemblyTypes, HttpStatus.OK);
        }
    */
    @JsonView(Views.UserViewPublic.class)
    @GetMapping("/{uuid}")
    public ResponseEntity<Recipient> getAssemblyType(
            @PathVariable String uuid,
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Profile-UUID") String currentProfileUuid,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid) {

        LOGGER.info("profile uuid {}", currentProfileUuid);
        LOGGER.info("localAuthority uuid {}", currentLocalAuthUuid);
        rights.forEach(right -> LOGGER.info("right {}", right.toString()));

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
            @RequestParam("email") String email) {

        LOGGER.info("profile uuid {}", currentProfileUuid);
        LOGGER.info("localAuthority uuid {}", currentLocalAuthUuid);
        rights.forEach(right -> LOGGER.info("right {}", right.toString()));

        if (!RightUtils.hasRight(rights, Arrays.asList(Right.CONVOCATION_ADMIN))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }


        Recipient recipient = recipientService.createFrom(firstname, lastname, email, currentLocalAuthUuid);
        recipient = recipientService.save(recipient);
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

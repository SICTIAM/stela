package fr.sictiam.stela.convocationservice.controller;

import com.fasterxml.jackson.annotation.JsonView;
import fr.sictiam.stela.convocationservice.model.LocalAuthority;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/convocation/local-authority")
public class LocalAuthorityRestController {

    private final static Logger LOGGER = LoggerFactory.getLogger(LocalAuthorityRestController.class);

    @Autowired
    private LocalAuthorityService localAuthorityService;

    @Autowired
    private RecipientService recipientService;

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

        if (!RightUtils.hasRight(rights, Arrays.asList(Right.CONVOCATION_ADMIN))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        LocalAuthority localAuthority = localAuthorityService.update(currentLocalAuthUuid, params);
        return new ResponseEntity<>(localAuthority, HttpStatus.OK);
    }
}

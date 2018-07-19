package fr.sictiam.stela.admin.controller;

import fr.sictiam.stela.admin.model.Instance;
import fr.sictiam.stela.admin.service.InstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/instance")
public class InstanceController {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstanceController.class);

    private final InstanceService instanceService;

    public InstanceController(InstanceService instanceService) {
        this.instanceService = instanceService;
    }

    @GetMapping
    public ResponseEntity<Instance> getInstanceParams() {
        return new ResponseEntity<>(instanceService.getInstance(), HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity updateInstanceParams(@RequestBody Instance instanceParams,
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin) {
        if (!isLocalAuthorityAdmin) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        instanceService.updateInstance(instanceParams);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/welcome-message")
    public ResponseEntity<String> getWelcomeMessage() {
        return new ResponseEntity<>(instanceService.getWelcomeMessage(), HttpStatus.OK);
    }

    @GetMapping("/legal-notice")
    public ResponseEntity<String> getLegalNotice() {
        return new ResponseEntity<>(instanceService.getLegalNotice(), HttpStatus.OK);
    }
}

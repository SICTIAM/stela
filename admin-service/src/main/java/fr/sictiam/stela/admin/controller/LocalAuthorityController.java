package fr.sictiam.stela.admin.controller;

import fr.sictiam.stela.admin.model.Module;
import fr.sictiam.stela.admin.model.ProvisioningRequest;
import fr.sictiam.stela.admin.service.LocalAuthorityService;
import fr.sictiam.stela.admin.service.OzwilloProvisioningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/admin/local-authority")
public class LocalAuthorityController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalAuthorityController.class);

    private final LocalAuthorityService localAuthorityService;
    private final OzwilloProvisioningService ozwilloProvisioningService;

    public LocalAuthorityController(LocalAuthorityService localAuthorityService,
                                    OzwilloProvisioningService ozwilloProvisioningService) {
        this.localAuthorityService = localAuthorityService;
        this.ozwilloProvisioningService = ozwilloProvisioningService;
    }

    @PostMapping
    public void create(@RequestBody @Valid ProvisioningRequest provisioningRequest) {
        LOGGER.debug("Got a provisioning request : {}", provisioningRequest);
        ozwilloProvisioningService.createNewInstance(provisioningRequest);
    }

    @PostMapping("/{uuid}/{module}")
    public void addModule(@PathVariable String uuid, @PathVariable Module module) {
        localAuthorityService.addModule(uuid, module);
    }

    @DeleteMapping("/{uuid}/{module}")
    public void removeModule(@PathVariable String uuid, @PathVariable Module module) {
        localAuthorityService.removeModule(uuid, module);
    }
}

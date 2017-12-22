package fr.sictiam.stela.admin.controller;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import fr.sictiam.stela.admin.model.LocalAuthority;
import fr.sictiam.stela.admin.model.Module;
import fr.sictiam.stela.admin.model.Profile;
import fr.sictiam.stela.admin.model.ProvisioningRequest;
import fr.sictiam.stela.admin.model.WorkGroup;
import fr.sictiam.stela.admin.model.UI.Views;
import fr.sictiam.stela.admin.service.LocalAuthorityService;
import fr.sictiam.stela.admin.service.OzwilloProvisioningService;
import fr.sictiam.stela.admin.service.ProfileService;
import fr.sictiam.stela.admin.service.WorkGroupService;

@RestController
@RequestMapping("/api/admin/local-authority")
public class LocalAuthorityController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalAuthorityController.class);

    private final LocalAuthorityService localAuthorityService;
    private final ProfileService profileService;
    private final WorkGroupService workGroupService;
    private final OzwilloProvisioningService ozwilloProvisioningService;

    public LocalAuthorityController(LocalAuthorityService localAuthorityService,
                                    ProfileService profileService, WorkGroupService workGroupService, OzwilloProvisioningService ozwilloProvisioningService) {
        this.localAuthorityService = localAuthorityService;
        this.profileService = profileService;
        this.workGroupService = workGroupService;
        this.ozwilloProvisioningService = ozwilloProvisioningService;
    }
    
    
    @PostMapping
    public void create(@RequestBody @Valid ProvisioningRequest provisioningRequest) {
        LOGGER.debug("Got a provisioning request : {}", provisioningRequest);
        ozwilloProvisioningService.createNewInstance(provisioningRequest);
    }

    @GetMapping("/current")
    @JsonView(Views.LocalAuthorityView.class)
    public LocalAuthority getCurrentLocalAuthority(@RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid) {
        return localAuthorityService.getByUuid(currentLocalAuthUuid);
    }

    @GetMapping
    @JsonView(Views.LocalAuthorityView.class)
    public List<LocalAuthority> getAllLocalAuthorities() {
        return localAuthorityService.getAll().stream().collect(Collectors.toList());
    }

    @GetMapping("/{uuid}")
    @JsonView(Views.LocalAuthorityView.class)
    public LocalAuthority getLocalAuthorityByUuid(@PathVariable String uuid) {
        return localAuthorityService.getByUuid(uuid);
    }


    @PostMapping("/current/{module}")
    public void addModule(@RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid, @PathVariable Module module) {
        localAuthorityService.addModule(currentLocalAuthUuid, module);
    }

    @DeleteMapping("/current/{module}")
    public void removeModule(@RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid, @PathVariable Module module) {
        localAuthorityService.removeModule(currentLocalAuthUuid, module);
    }
    
    @GetMapping("/{uuid}/agent/{agentUuid}")
    @JsonView(Views.ProfileView.class)
    public Profile getProfile(@PathVariable String uuid, @PathVariable String agentUuid) {
        return profileService.getByAgentAndLocalAuthority(uuid, agentUuid);
    }

    @GetMapping("/{uuid}/group")
    @JsonView(Views.WorkGroupView.class)
    public List<WorkGroup> getAllGroupByLocalAuthority(@PathVariable String uuid) {
        return workGroupService.getAllByLocalAuthority(uuid).stream().collect(Collectors.toList());
    }

    @PostMapping("/{uuid}/group")
    @JsonView(Views.WorkGroupView.class)
    public WorkGroup addGroup(@PathVariable String uuid, @RequestBody String groupName) {
        WorkGroup workGroup = workGroupService.create(new WorkGroup(localAuthorityService.getByUuid(uuid), groupName));
        return workGroup;
    }

    @DeleteMapping("/{uuid}/group/{groupUuid}")
    public void deleteGroup(@PathVariable String groupUuid) {
        workGroupService.deleteGroup(groupUuid);
    }

    @PostMapping("/{uuid}/{module}")
    public void addModuleByUuid(@PathVariable String uuid, @PathVariable Module module) {
        localAuthorityService.addModule(uuid, module);
    }

    @DeleteMapping("/{uuid}/{module}")
    public void removeModuleByUuid(@PathVariable String uuid, @PathVariable Module module) {
        localAuthorityService.removeModule(uuid, module);
    }
}

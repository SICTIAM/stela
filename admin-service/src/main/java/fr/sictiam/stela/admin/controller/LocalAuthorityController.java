package fr.sictiam.stela.admin.controller;

import com.fasterxml.jackson.annotation.JsonView;
import fr.sictiam.stela.admin.model.LocalAuthority;
import fr.sictiam.stela.admin.model.Module;
import fr.sictiam.stela.admin.model.OzwilloInstanceInfo;
import fr.sictiam.stela.admin.model.Profile;
import fr.sictiam.stela.admin.model.UI.LocalAuthorityResultsUI;
import fr.sictiam.stela.admin.model.UI.Views;
import fr.sictiam.stela.admin.model.UI.WorkGroupUI;
import fr.sictiam.stela.admin.model.WorkGroup;
import fr.sictiam.stela.admin.service.LocalAuthorityService;
import fr.sictiam.stela.admin.service.ProfileService;
import fr.sictiam.stela.admin.service.WorkGroupService;
import fr.sictiam.stela.admin.service.exceptions.NotFoundException;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/local-authority")
public class LocalAuthorityController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalAuthorityController.class);

    private final LocalAuthorityService localAuthorityService;
    private final ProfileService profileService;
    private final WorkGroupService workGroupService;

    public LocalAuthorityController(LocalAuthorityService localAuthorityService, ProfileService profileService,
            WorkGroupService workGroupService) {
        this.localAuthorityService = localAuthorityService;
        this.profileService = profileService;
        this.workGroupService = workGroupService;
    }

    @GetMapping("/current")
    @JsonView(Views.LocalAuthorityView.class)
    public ResponseEntity<LocalAuthority> getCurrentLocalAuthority(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid) {
        if (!isLocalAuthorityAdmin) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(localAuthorityService.getByUuid(currentLocalAuthUuid), HttpStatus.OK);
    }

    @GetMapping
    @JsonView(Views.LocalAuthorityView.class)
    public ResponseEntity<LocalAuthorityResultsUI> getAllLocalAuthorities(
            @RequestParam(value = "limit", required = false, defaultValue = "25") Integer limit,
            @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
            @RequestParam(value = "column", required = false, defaultValue = "name") String column,
            @RequestParam(value = "direction", required = false, defaultValue = "ASC") Sort.Direction direction,
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin) {
        if (!isLocalAuthorityAdmin) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        List<LocalAuthority> localAuthorities = localAuthorityService.getAllWithPagination(limit, offset, column,
                direction);
        Long count = localAuthorityService.countAll();
        return new ResponseEntity<>(new LocalAuthorityResultsUI(count, localAuthorities), HttpStatus.OK);
    }

    @GetMapping("/all")
    @JsonView(Views.LocalAuthorityViewBasic.class)
    public List<LocalAuthority> getAllBasicLocalAuthorities() {
        return localAuthorityService.getAll();
    }

    @GetMapping("/{uuid}")
    @JsonView(Views.LocalAuthorityView.class)
    public ResponseEntity<LocalAuthority> getLocalAuthorityByUuid(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @PathVariable String uuid) {
        if (!isLocalAuthorityAdmin) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(localAuthorityService.getByUuid(uuid), HttpStatus.OK);
    }

    @GetMapping("/instance/{slugName}")
    public OzwilloInstanceInfo getInstanceInfoBySlugName(@PathVariable String slugName) {
        // as soon as an instance is stopped, consider it does no longer exist
        // even if it seems that, for a STOPPED instance, we don't even get to this
        // point
        // as the client_id is rejected by the kernel when trying to authenticate
        return localAuthorityService.getBySlugName(slugName)
                .filter(localAuthority -> localAuthority.getStatus().equals(LocalAuthority.Status.RUNNING))
                .map(LocalAuthority::getOzwilloInstanceInfo)
                .orElseThrow(() -> new NotFoundException("No local authority found for slug " + slugName));
    }

    @PostMapping("/current/{module}")
    public ResponseEntity addModule(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @PathVariable Module module) {
        if (!isLocalAuthorityAdmin) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        localAuthorityService.addModule(currentLocalAuthUuid, module);
        return new ResponseEntity(HttpStatus.OK);
    }

    @DeleteMapping("/current/{module}")
    public ResponseEntity removeModule(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @PathVariable Module module) {
        if (!isLocalAuthorityAdmin) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        localAuthorityService.removeModule(currentLocalAuthUuid, module);
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/{uuid}/agent/{agentUuid}")
    @JsonView(Views.ProfileView.class)
    public ResponseEntity<Profile> getProfile(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @PathVariable String uuid, @PathVariable String agentUuid) {
        if (!isLocalAuthorityAdmin) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(profileService.getByAgentAndLocalAuthority(uuid, agentUuid), HttpStatus.OK);
    }

    @GetMapping("/{uuid}/group")
    @JsonView(Views.WorkGroupView.class)
    public ResponseEntity<List<WorkGroup>> getAllGroupByLocalAuthority(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @PathVariable String uuid) {
        if (!isLocalAuthorityAdmin) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(workGroupService.getAllByLocalAuthority(uuid), HttpStatus.OK);
    }

    @PostMapping("/{uuid}/group")
    @JsonView(Views.WorkGroupView.class)
    public ResponseEntity<WorkGroup> newGroup(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @PathVariable String uuid, @RequestBody WorkGroupUI workGroupUI) {
        if (!isLocalAuthorityAdmin) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(workGroupService.createFromUI(workGroupUI, uuid), HttpStatus.OK);
    }

    @GetMapping("/{localAuthorityUuid}/group/{uuid}")
    @JsonView(Views.WorkGroupView.class)
    public ResponseEntity<WorkGroup> getGroup(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @PathVariable String uuid) {
        if (!isLocalAuthorityAdmin) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        WorkGroup workGroup = workGroupService.getByUuid(uuid);
        return new ResponseEntity<>(workGroup, HttpStatus.OK);
    }

    @PatchMapping("/{localAuthorityUuid}/group/{uuid}")
    public ResponseEntity updateGroup(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @PathVariable String uuid, @RequestBody WorkGroupUI workGroupUI) {
        if (!isLocalAuthorityAdmin) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        WorkGroup workGroup = workGroupService.getByUuid(uuid);
        try {
            BeanUtils.copyProperties(workGroup, workGroupUI);
        } catch (Exception e) {
            LOGGER.error("Error while updating properties: {}", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        workGroupService.update(workGroup);
        return new ResponseEntity(HttpStatus.OK);
    }

    @DeleteMapping("/{uuid}/group/{groupUuid}")
    public ResponseEntity deleteGroup(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @PathVariable String groupUuid) {
        if (!isLocalAuthorityAdmin) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        workGroupService.deleteGroup(groupUuid);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/{uuid}/{module}")
    public ResponseEntity addModuleByUuid(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @PathVariable String uuid, @PathVariable Module module) {
        if (!isLocalAuthorityAdmin) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        localAuthorityService.addModule(uuid, module);
        return new ResponseEntity(HttpStatus.OK);
    }

    @DeleteMapping("/{uuid}/{module}")
    public ResponseEntity removeModuleByUuid(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @PathVariable String uuid, @PathVariable Module module) {
        if (!isLocalAuthorityAdmin) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        localAuthorityService.removeModule(uuid, module);
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/{uuid}/profiles")
    @JsonView(Views.ProfileView.class)
    public ResponseEntity<List<Profile>> getProfiles(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @PathVariable String uuid) {
        if (!isLocalAuthorityAdmin) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(profileService.getProfilesByLocalAuthorityUuid(uuid), HttpStatus.OK);
    }
}

package fr.sictiam.stela.admin.controller;

import com.fasterxml.jackson.annotation.JsonView;
import fr.sictiam.stela.admin.model.*;
import fr.sictiam.stela.admin.model.UI.Views;
import fr.sictiam.stela.admin.model.UI.WorkGroupUI;
import fr.sictiam.stela.admin.service.LocalAuthorityService;
import fr.sictiam.stela.admin.service.ProfileService;
import fr.sictiam.stela.admin.service.WorkGroupService;
import fr.sictiam.stela.admin.service.exceptions.NotFoundException;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    @GetMapping("/instance-id/{instaceId}")
    public ResponseEntity<String> getSlugByInstanceId(@PathVariable String instaceId) {
        Optional<LocalAuthority> opt = localAuthorityService.getByInstanceId(instaceId);
        return opt.map(localAuthority -> new ResponseEntity<>(localAuthority.getSlugName(), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/current")
    @JsonView(Views.LocalAuthorityView.class)
    public ResponseEntity<LocalAuthority> getCurrentLocalAuthority(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute("STELA-Current-Agent-UUID") String agentUuid) {
        if (!isLocalAuthorityAdmin || !localAuthorityService.isAgentAdmin(agentUuid, currentLocalAuthUuid)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(localAuthorityService.getByUuid(currentLocalAuthUuid), HttpStatus.OK);
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
            @RequestAttribute("STELA-Current-Agent-UUID") String agentUuid,
            @PathVariable String uuid) {

        if (!isLocalAuthorityAdmin || !localAuthorityService.isAgentAdmin(agentUuid, uuid)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(localAuthorityService.getByUuid(uuid), HttpStatus.OK);
    }

    @GetMapping("/instance/slug-name/{slugName}")
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

    @GetMapping("/instance/instance-id/{instanceId}")
    public OzwilloInstanceInfo getInstanceInfoByInstanceId(@PathVariable String instanceId) {
        return localAuthorityService.getByInstanceId(instanceId)
                .filter(localAuthority -> localAuthority.getStatus().equals(LocalAuthority.Status.RUNNING))
                .map(LocalAuthority::getOzwilloInstanceInfo)
                .orElseThrow(() -> new NotFoundException("No local authority found for instance_id " + instanceId));
    }

    @PostMapping("/current/{module}")
    public ResponseEntity addModule(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute("STELA-Current-Agent-UUID") String agentUuid,
            @PathVariable Module module) {
        if (!isLocalAuthorityAdmin || !localAuthorityService.isAgentAdmin(agentUuid, currentLocalAuthUuid)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        localAuthorityService.addModule(currentLocalAuthUuid, module);
        return new ResponseEntity(HttpStatus.OK);
    }

    @DeleteMapping("/current/{module}")
    public ResponseEntity removeModule(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute("STELA-Current-Agent-UUID") String agentUuid,
            @PathVariable Module module) {
        if (!isLocalAuthorityAdmin || !localAuthorityService.isAgentAdmin(agentUuid, currentLocalAuthUuid)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        localAuthorityService.removeModule(currentLocalAuthUuid, module);
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/{uuid}/agent/{agentUuid}")
    @JsonView(Views.ProfileView.class)
    public ResponseEntity<Profile> getProfile(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @RequestAttribute("STELA-Current-Agent-UUID") String currentAgentUuid,
            @PathVariable String uuid, @PathVariable String agentUuid) {
        if (!isLocalAuthorityAdmin || !localAuthorityService.isAgentAdmin(currentAgentUuid, uuid)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(profileService.getByAgentAndLocalAuthority(uuid, agentUuid), HttpStatus.OK);
    }

    @GetMapping("/current/agent/{agentUuid}")
    @JsonView(Views.ProfileView.class)
    public ResponseEntity<Profile> getProfileFromCurrent(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute("STELA-Current-Agent-UUID") String currentAgentUuid,
            @PathVariable String agentUuid) {
        if (!isLocalAuthorityAdmin || !localAuthorityService.isAgentAdmin(currentAgentUuid, currentLocalAuthUuid)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(profileService.getByAgentAndLocalAuthority(currentLocalAuthUuid, agentUuid),
                HttpStatus.OK);
    }

    @GetMapping("/{uuid}/group")
    @JsonView(Views.WorkGroupView.class)
    public ResponseEntity<List<WorkGroup>> getAllGroupByLocalAuthority(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @RequestAttribute("STELA-Current-Agent-UUID") String currentAgentUuid,
            @PathVariable String uuid) {
        if (!isLocalAuthorityAdmin || !localAuthorityService.isAgentAdmin(currentAgentUuid, uuid)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(workGroupService.getAllByLocalAuthority(uuid), HttpStatus.OK);
    }

    @GetMapping("/{uuid}/{moduleName}/group")
    @JsonView(Views.WorkGroupView.class)
    public ResponseEntity<List<WorkGroup>> getAllGroupByLocalAuthorityAndModule(@PathVariable String uuid,
            @PathVariable String moduleName) {
        return new ResponseEntity<>(workGroupService.getAllByLocalAuthorityAndModule(uuid, moduleName), HttpStatus.OK);
    }

    @GetMapping("/current/group")
    @JsonView(Views.WorkGroupView.class)
    public ResponseEntity<List<WorkGroup>> getAllGroupForCurrent(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute("STELA-Current-Agent-UUID") String currentAgentUuid) {
        if (!isLocalAuthorityAdmin || !localAuthorityService.isAgentAdmin(currentAgentUuid, currentLocalAuthUuid)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(workGroupService.getAllByLocalAuthority(currentLocalAuthUuid), HttpStatus.OK);
    }

    @PostMapping("/{uuid}/group")
    @JsonView(Views.WorkGroupView.class)
    public ResponseEntity<WorkGroup> newGroupFromFront(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @RequestAttribute("STELA-Current-Agent-UUID") String currentAgentUuid,
            @PathVariable String uuid, @RequestBody WorkGroupUI workGroupUI) {
        if (!isLocalAuthorityAdmin || !localAuthorityService.isAgentAdmin(currentAgentUuid, uuid)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(workGroupService.createFromUI(workGroupUI, uuid), HttpStatus.OK);
    }

    @PostMapping("/{uuid}/group/{name}")
    @JsonView(Views.WorkGroupView.class)
    public ResponseEntity<String> newGroupFromModules(
            @PathVariable String uuid, @PathVariable String name, @RequestBody Set<String> rights) {
        return new ResponseEntity<>(workGroupService.createFromModules(name, rights, uuid).getUuid(), HttpStatus.OK);
    }

    @PostMapping("/current/group")
    @JsonView(Views.WorkGroupView.class)
    public ResponseEntity<WorkGroup> newGroupForCurrent(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute("STELA-Current-Agent-UUID") String currentAgentUuid,
            @RequestBody WorkGroupUI workGroupUI) {
        if (!isLocalAuthorityAdmin || !localAuthorityService.isAgentAdmin(currentAgentUuid, currentLocalAuthUuid)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(workGroupService.createFromUI(workGroupUI, currentLocalAuthUuid), HttpStatus.OK);
    }

    @GetMapping("/group/{uuid}")
    @JsonView(Views.WorkGroupView.class)
    public ResponseEntity<WorkGroup> getGroup(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute("STELA-Current-Agent-UUID") String currentAgentUuid,
            @PathVariable String uuid) {
        if (!isLocalAuthorityAdmin || !localAuthorityService.isAgentAdmin(currentAgentUuid, currentLocalAuthUuid)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        WorkGroup workGroup = workGroupService.getByUuid(uuid);
        return new ResponseEntity<>(workGroup, HttpStatus.OK);
    }

    @PatchMapping("/group/{uuid}")
    @JsonView(Views.WorkGroupView.class)
    public ResponseEntity<WorkGroup> updateGroup(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute("STELA-Current-Agent-UUID") String currentAgentUuid,
            @PathVariable String uuid, @RequestBody WorkGroupUI workGroupUI) {
        if (!isLocalAuthorityAdmin || !localAuthorityService.isAgentAdmin(currentAgentUuid, currentLocalAuthUuid)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        WorkGroup workGroup = workGroupService.getByUuid(uuid);
        try {
            BeanUtils.copyProperties(workGroup, workGroupUI);
        } catch (Exception e) {
            LOGGER.error("Error while updating properties: {}", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(workGroupService.update(workGroup), HttpStatus.OK);
    }

    @DeleteMapping("/group/{uuid}")
    public ResponseEntity deleteGroup(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute("STELA-Current-Agent-UUID") String currentAgentUuid,
            @PathVariable String uuid) {
        if (!isLocalAuthorityAdmin || !localAuthorityService.isAgentAdmin(currentAgentUuid, currentLocalAuthUuid)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        workGroupService.deleteGroup(uuid);
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/certificates/{uuid}")
    public ResponseEntity<Certificate> getLocalAuthorityCertificate(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute("STELA-Current-Agent-UUID") String currentAgentUuid,
            @PathVariable String uuid) {
        if (!isLocalAuthorityAdmin || !localAuthorityService.isAgentAdmin(currentAgentUuid, currentLocalAuthUuid)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(localAuthorityService.getCertificate(uuid), HttpStatus.OK);
    }

    @PostMapping("/current/certificates")
    public ResponseEntity addCurrentLocalAuthorityCertificate(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute("STELA-Current-Agent-UUID") String currentAgentUuid,
            @RequestParam("file") MultipartFile file) {
        if (!isLocalAuthorityAdmin || !localAuthorityService.isAgentAdmin(currentAgentUuid, currentLocalAuthUuid)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return addCertificate(currentLocalAuthUuid, file);
    }

    @PostMapping("/{uuid}/certificates")
    public ResponseEntity addLocalAuthorityCertificate(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute("STELA-Current-Agent-UUID") String currentAgentUuid,
            @PathVariable String uuid, @RequestParam("file") MultipartFile file) {
        if (!isLocalAuthorityAdmin || !localAuthorityService.isAgentAdmin(currentAgentUuid, currentLocalAuthUuid)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return addCertificate(uuid, file);
    }

    private ResponseEntity addCertificate(String uuid, MultipartFile file) {
        try {
            localAuthorityService.addCertificate(uuid, file);
            return new ResponseEntity(HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("admin.invalid_certificate", HttpStatus.NOT_ACCEPTABLE);
        } catch (IOException | CertificateException e) {
            return new ResponseEntity<>("admin.error_certificate", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/current/certificates/{uuid}")
    public ResponseEntity deleteCurrentLocalAuthorityCertificate(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute("STELA-Current-Agent-UUID") String currentAgentUuid,
            @PathVariable String uuid) {
        if (!isLocalAuthorityAdmin || !localAuthorityService.isAgentAdmin(currentAgentUuid, currentLocalAuthUuid)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        localAuthorityService.deleteCertificate(currentLocalAuthUuid, uuid);
        return new ResponseEntity(HttpStatus.OK);
    }

    @DeleteMapping("/{localAuthorityUuid}/certificates/{uuid}")
    public ResponseEntity deleteLocalAuthorityCertificate(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @RequestAttribute("STELA-Current-Agent-UUID") String currentAgentUuid,
            @PathVariable String localAuthorityUuid, @PathVariable String uuid) {
        if (!isLocalAuthorityAdmin || !localAuthorityService.isAgentAdmin(currentAgentUuid, localAuthorityUuid)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        localAuthorityService.deleteCertificate(localAuthorityUuid, uuid);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/{uuid}/{module}")
    public ResponseEntity addModuleByUuid(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @RequestAttribute("STELA-Current-Agent-UUID") String currentAgentUuid,
            @PathVariable String uuid, @PathVariable Module module) {
        if (!isLocalAuthorityAdmin || !localAuthorityService.isAgentAdmin(currentAgentUuid, uuid)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        localAuthorityService.addModule(uuid, module);
        return new ResponseEntity(HttpStatus.OK);
    }

    @DeleteMapping("/{uuid}/{module}")
    public ResponseEntity removeModuleByUuid(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @RequestAttribute("STELA-Current-Agent-UUID") String currentAgentUuid,
            @PathVariable String uuid, @PathVariable Module module) {
        if (!isLocalAuthorityAdmin || !localAuthorityService.isAgentAdmin(currentAgentUuid, uuid)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        localAuthorityService.removeModule(uuid, module);
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/certificate/{serial}/{issuer}")
    public ResponseEntity<LocalAuthority> getByCertificate(
            @PathVariable String serial,
            @PathVariable String issuer) {
        Optional<LocalAuthority> opt = localAuthorityService.getByCertificate(serial, issuer);
        return opt.map(localAuthority -> new ResponseEntity<>(localAuthority, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/{uuid}/siret")
    public String getSiret(@PathVariable String uuid) {

        LocalAuthority localAuthority = localAuthorityService.getByUuid(uuid);
        String dcId = localAuthority.getOzwilloInstanceInfo().getDcId();
        return dcId.substring(dcId.lastIndexOf('/') + 1);
    }

    @GetMapping("/{siren}/accessToken")
    public ResponseEntity<String> getAccessToken(@PathVariable String siren) {
        Optional<TokenResponse> tokenResponse = localAuthorityService.getAccessTokenFromKernel(siren);

        return tokenResponse
                .map(tokenResponse1 -> new ResponseEntity<>(tokenResponse1.getAccessToken(), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @GetMapping("/{siren}/dcId")
    public ResponseEntity<String> getDcId(@PathVariable String siren) {
        Optional<LocalAuthority> localAuthority =  localAuthorityService.findBySiren(siren);

        return localAuthority
                .map(localAuthority1 -> new ResponseEntity<>(localAuthority1.getOzwilloInstanceInfo().getDcId(), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}

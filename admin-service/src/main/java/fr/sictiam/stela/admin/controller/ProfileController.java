package fr.sictiam.stela.admin.controller;

import com.fasterxml.jackson.annotation.JsonView;
import fr.sictiam.stela.admin.model.Module;
import fr.sictiam.stela.admin.model.Profile;
import fr.sictiam.stela.admin.model.WorkGroup;
import fr.sictiam.stela.admin.model.UI.ProfileRights;
import fr.sictiam.stela.admin.model.UI.ProfileUI;
import fr.sictiam.stela.admin.model.UI.Views;
import fr.sictiam.stela.admin.service.ProfileService;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/admin/profile")
public class ProfileController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileController.class);

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PutMapping("/{uuid}/rights")
    public ResponseEntity updateProfileRights(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @PathVariable String uuid, @RequestBody ProfileRights profileRights) {
        if (!isLocalAuthorityAdmin) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        profileService.updateProfileRights(uuid, profileRights);
        return new ResponseEntity(HttpStatus.OK);
    }

    @JsonView(Views.WorkGroupViewPublic.class)
    @GetMapping("/groups")
    public Set<WorkGroup> getProfileGroups(@RequestAttribute("STELA-Current-Profile-UUID") String profile) {
        return profileService.getByUuid(profile).getGroups();
    }

    @GetMapping
    @JsonView(Views.ProfileView.class)
    public Profile getCurrentProfile(@RequestAttribute("STELA-Current-Profile-UUID") String profile) {
        return profileService.getByUuid(profile);
    }

    @GetMapping("/{uuid}")
    @JsonView(Views.ProfileView.class)
    public Profile getCurrentProfileByUuid(@PathVariable String uuid) {
        return profileService.getByUuid(uuid);
    }

    @GetMapping("/local-authority/{siren}/{email}")
    @JsonView(Views.ProfileView.class)
    public Profile getByLocalAuthoritySirenAndEmail(@PathVariable String siren, @PathVariable String email) {
        return profileService.getByLocalAuthoritySirenAndEmail(siren, email);
    }

    @GetMapping("/{uuid}/slug")
    public String getSlugForProfile(@PathVariable String uuid) {
        return profileService.getByUuid(uuid).getLocalAuthority().getSlugName();
    }

    // TODO: Fix Rights on this endpoint
    // one user do not need any right to modify all his profile but should not be
    // able to modify all profiles
    @PatchMapping("/{uuid}")
    public ResponseEntity<?> updateProfile(@PathVariable String uuid, @RequestBody ProfileUI profileUI) {
        Profile profile = profileService.getByUuid(uuid);
        try {
            BeanUtils.copyProperties(profile, profileUI);
        } catch (Exception e) {
            LOGGER.error("Error while updating properties: {}", e);
            return new ResponseEntity<Object>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        profileService.createOrUpdate(profile);
        return new ResponseEntity<Object>(HttpStatus.OK);
    }

    @GetMapping("/modules")
    public Set<Module> getProfileModules(@RequestAttribute("STELA-Current-Profile-UUID") String profile) {
        return profileService.getByUuid(profile).getLocalAuthority().getActivatedModules();
    }
}

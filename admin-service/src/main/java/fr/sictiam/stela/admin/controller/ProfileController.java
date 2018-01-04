package fr.sictiam.stela.admin.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import fr.sictiam.stela.admin.model.Profile;
import fr.sictiam.stela.admin.model.UI.Views;
import fr.sictiam.stela.admin.service.ProfileService;

@RestController
@RequestMapping("/api/admin/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PutMapping("/{uuid}/group")
    public void updateGroups(@PathVariable String uuid, @RequestBody List<String> groupUuids) {
        profileService.updateGroups(uuid, groupUuids);
    }

    @GetMapping
    @JsonView(Views.ProfileView.class)
    public Profile getCurrentProfile(@RequestAttribute("STELA-Current-Profile") String profile) {
        return profileService.getByUuid(profile);
    }

    @GetMapping("/{uuid}/slug")
    public String getSlugForProfile(@PathVariable String uuid) {
        return profileService.getByUuid(uuid).getLocalAuthority().getSlugName();
    }
}

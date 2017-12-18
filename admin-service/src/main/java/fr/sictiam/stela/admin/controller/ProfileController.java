package fr.sictiam.stela.admin.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.sictiam.stela.admin.model.UI.ProfileUI;
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
    public ProfileUI getCurrentProfile(@RequestAttribute("CurrentProfile") String profile) {
        return new ProfileUI(profileService.getByUuid(profile));
    }
}

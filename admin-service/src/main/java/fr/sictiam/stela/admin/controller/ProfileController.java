package fr.sictiam.stela.admin.controller;

import fr.sictiam.stela.admin.service.ProfileService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

}

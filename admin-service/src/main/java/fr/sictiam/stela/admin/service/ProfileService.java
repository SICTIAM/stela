package fr.sictiam.stela.admin.service;

import fr.sictiam.stela.admin.dao.ProfileRepository;
import fr.sictiam.stela.admin.model.Profile;
import fr.sictiam.stela.admin.model.UI.ProfileRights;
import fr.sictiam.stela.admin.model.WorkGroup;
import fr.sictiam.stela.admin.service.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final WorkGroupService workGroupService;
    private final LocalAuthorityService localAuthorityService;

    public ProfileService(ProfileRepository profileRepository, WorkGroupService workGroupService,
            LocalAuthorityService localAuthorityService) {
        this.profileRepository = profileRepository;
        this.workGroupService = workGroupService;
        this.localAuthorityService = localAuthorityService;
    }

    @Transactional
    public Profile createOrUpdate(Profile profile) {

        return profileRepository.save(profile);
    }

    public Profile getByLocalAuthoritySirenAndEmail(String siren, String email) {
        return profileRepository.findByLocalAuthority_SirenAndAgent_EmailIgnoreCase(siren, email)
                .orElseThrow(() -> new NotFoundException("notifications.admin.agent_not_found"));
    }

    public Profile getByAgentAndLocalAuthority(String localAuthorityUuid, String agentUuid) {
        return profileRepository.findByLocalAuthority_UuidAndAgent_Uuid(localAuthorityUuid, agentUuid)
                .orElseThrow(() -> new NotFoundException("notifications.admin.agent_not_found"));
    }

    public Profile getByUuid(String uuid) {
        return profileRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException("notifications.admin.profile_not_found"));
    }

    public void updateProfileRights(String profileUuid, ProfileRights profileRights) {
        Profile profile = getByUuid(profileUuid);
        profile.getGroups().forEach(workGroup -> workGroup.getProfiles().remove(profile));

        Set<WorkGroup> groups = profileRights.getGroupUuids().stream().map(workGroupService::getByUuid)
                .collect(Collectors.toSet());
        groups.forEach(workGroup -> workGroup.getProfiles().add(profile));
        profile.setGroups(groups);
        profile.setAdmin(profileRights.isAdmin());
        profileRepository.save(profile);
        localAuthorityService.createOrUpdate(profile.getLocalAuthority());
    }

    public List<Profile> getProfilesByLocalAuthorityUuid(String uuid) {
        return profileRepository.findByLocalAuthority_Uuid(uuid);
    }
}

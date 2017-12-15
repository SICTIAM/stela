package fr.sictiam.stela.admin.service;

import fr.sictiam.stela.admin.model.WorkGroup;
import fr.sictiam.stela.admin.service.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fr.sictiam.stela.admin.dao.ProfileRepository;
import fr.sictiam.stela.admin.model.Profile;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProfileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileService.class);

    private final ProfileRepository profileRepository;
    private final WorkGroupService workGroupService;

    public ProfileService(ProfileRepository profileRepository, WorkGroupService workGroupService) {
        this.profileRepository = profileRepository;
        this.workGroupService = workGroupService;
    }
    
    public Profile create(Profile profile) {
        return profileRepository.save(profile);
    }

    public Profile getByAgentAndLocalAuthority(String localAuthorityUuid, String agentUuid) {
        return profileRepository.findByLocalAuthority_UuidAndAgent_Uuid(localAuthorityUuid, agentUuid)
                .orElseThrow(() -> new NotFoundException("notifications.admin.agent_not_found"));
    }

    public Profile getByUuid(String uuid) {
        return profileRepository.findByUuid(uuid).orElseThrow(() -> new NotFoundException("notifications.admin.profile_not_found"));
    }

    public void updateGroups(String profileUuid, List<String> uuids) {
        Profile profile = getByUuid(profileUuid);
        profile.getGroups().forEach(workGroup -> workGroup.getProfiles().remove(profile));

        Set<WorkGroup> groups = uuids.stream().map(workGroupService::getByUuid).collect(Collectors.toSet());
        groups.forEach(workGroup -> workGroup.getProfiles().add(profile));
        profile.setGroups(groups);
        profileRepository.save(profile);
    }
}

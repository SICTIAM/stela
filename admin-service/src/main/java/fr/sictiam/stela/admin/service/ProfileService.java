package fr.sictiam.stela.admin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fr.sictiam.stela.admin.dao.ProfileRepository;
import fr.sictiam.stela.admin.model.Profile;

@Service
public class ProfileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileService.class);

    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }
    
    public Profile create(Profile profile) {
        return profileRepository.save(profile);
    }
}

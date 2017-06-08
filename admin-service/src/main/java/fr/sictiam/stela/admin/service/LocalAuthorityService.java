package fr.sictiam.stela.admin.service;

import fr.sictiam.stela.admin.dao.LocalAuthorityRepository;
import fr.sictiam.stela.admin.model.LocalAuthority;
import fr.sictiam.stela.admin.model.Module;
import org.springframework.stereotype.Service;

@Service
public class LocalAuthorityService {

    private final LocalAuthorityRepository localAuthorityRepository;

    public LocalAuthorityService(LocalAuthorityRepository localAuthorityRepository) {
        this.localAuthorityRepository = localAuthorityRepository;
    }

    public LocalAuthority create(LocalAuthority localAuthority) {
        return localAuthorityRepository.save(localAuthority);
    }

    public void addModule(String uuid, Module module) {
        LocalAuthority localAuthority = localAuthorityRepository.findByUuid(uuid);
        localAuthority.addModule(module);
        localAuthorityRepository.save(localAuthority);
    }

    public void removeModule(String uuid, Module module) {
        LocalAuthority localAuthority = localAuthorityRepository.findByUuid(uuid);
        localAuthority.removeModule(module);
        localAuthorityRepository.save(localAuthority);
    }
}

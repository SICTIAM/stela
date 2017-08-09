package fr.sictiam.stela.admin.service;

import fr.sictiam.stela.admin.dao.LocalAuthorityRepository;
import fr.sictiam.stela.admin.model.LocalAuthority;
import fr.sictiam.stela.admin.model.Module;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LocalAuthorityService {

    private final LocalAuthorityRepository localAuthorityRepository;

    public LocalAuthorityService(LocalAuthorityRepository localAuthorityRepository) {
        this.localAuthorityRepository = localAuthorityRepository;
    }

    public LocalAuthority create(LocalAuthority localAuthority) {
        localAuthority = localAuthorityRepository.save(localAuthority);

        // TODO : send a message to the bus

        return localAuthority;
    }

    public LocalAuthority modify(LocalAuthority localAuthority) {
        return localAuthorityRepository.save(localAuthority);
    }

    public void addModule(String uuid, Module module) {
        LocalAuthority localAuthority = localAuthorityRepository.getOne(uuid);
        localAuthority.addModule(module);
        localAuthorityRepository.save(localAuthority);
    }

    public void removeModule(String uuid, Module module) {
        LocalAuthority localAuthority = localAuthorityRepository.getOne(uuid);
        localAuthority.removeModule(module);
        localAuthorityRepository.save(localAuthority);
    }

    public Optional<LocalAuthority> findByName(String name) {
        return localAuthorityRepository.findByName(name);
    }

    public Optional<LocalAuthority> findBySiren(String siren) {
        return localAuthorityRepository.findBySiren(siren);
    }
}

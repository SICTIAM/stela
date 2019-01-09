package fr.sictiam.stela.convocationservice.service;

import fr.sictiam.stela.convocationservice.dao.LocalAuthorityRepository;
import fr.sictiam.stela.convocationservice.model.LocalAuthority;
import fr.sictiam.stela.convocationservice.model.event.LocalAuthorityEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.io.IOException;
import java.util.Optional;

@Service
public class LocalAuthorityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalAuthorityService.class);

    private final LocalAuthorityRepository localAuthorityRepository;

    @Autowired
    public LocalAuthorityService(LocalAuthorityRepository localAuthorityRepository) {
        this.localAuthorityRepository = localAuthorityRepository;
    }

    public LocalAuthority createOrUpdate(LocalAuthority localAuthority) {
        return localAuthorityRepository.save(localAuthority);
    }

    public void delete(LocalAuthority localAuthority) {
        localAuthorityRepository.delete(localAuthority);
    }

    public LocalAuthority getByUuid(String uuid) {
        return localAuthorityRepository.findByUuid(uuid).get();
    }

    public Optional<LocalAuthority> getByName(String name) {
        return localAuthorityRepository.findByName(name);
    }

    public Optional<LocalAuthority> getBySiren(String siren) {
        return localAuthorityRepository.findBySiren(siren);
    }

    @Transactional
    public void handleEvent(LocalAuthorityEvent event) throws IOException {
        LocalAuthority localAuthority = localAuthorityRepository.findByUuid(event.getUuid())
                .orElse(new LocalAuthority(event.getUuid(), event.getName(), event.getSlugName(), event.getSiren()));

        // Update existing local authorities with new slug name
        localAuthority.setSlugName(event.getSlugName());
        
        createOrUpdate(localAuthority);
    }

}

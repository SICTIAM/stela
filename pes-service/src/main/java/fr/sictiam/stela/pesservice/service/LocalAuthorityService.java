package fr.sictiam.stela.pesservice.service;

import fr.sictiam.stela.pesservice.dao.LocalAuthorityRepository;
import fr.sictiam.stela.pesservice.model.LocalAuthority;
import fr.sictiam.stela.pesservice.model.event.LocalAuthorityEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public List<LocalAuthority> getAll() {
        List<LocalAuthority> localAuthorities = localAuthorityRepository.findAll();
        localAuthorities.sort(Comparator.comparing(LocalAuthority::getName, String.CASE_INSENSITIVE_ORDER));
        return localAuthorities;
    }

    public List<LocalAuthority> getAllActive() {
        List<LocalAuthority> localAuthorities = localAuthorityRepository.findAllByActiveTrue();
        localAuthorities.sort(Comparator.comparing(LocalAuthority::getName, String.CASE_INSENSITIVE_ORDER));
        return localAuthorities;
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

    public Optional<LocalAuthority> getBySirenOrSirens(String siren) {
        return localAuthorityRepository.findByActiveTrueAndSirenEqualsOrSirens(siren, siren);
    }

    @Transactional
    public void handleEvent(LocalAuthorityEvent event) throws IOException {
        LocalAuthority localAuthority = localAuthorityRepository.findByUuid(event.getUuid())
                .orElse(new LocalAuthority(event.getUuid(), event.getName(), event.getSiren()));

        if (event.getActivatedModules().contains("PES")) {
            localAuthority.setActive(true);
            List<LocalAuthority> allLocalAuthorities = getAllActive();
            allLocalAuthorities.forEach(localAuth -> {
                if (localAuth.getSirens().stream().anyMatch(siren -> localAuthority.getSiren().equals(localAuth.getSiren()))) {
                    localAuth.setSirens(
                            localAuth.getSirens().stream()
                                    .filter(siren -> !localAuthority.getSiren().equals(localAuth.getSiren()))
                                    .collect(Collectors.toList())
                    );
                    createOrUpdate(localAuth);
                }
            });
        }
        createOrUpdate(localAuthority);
    }

}

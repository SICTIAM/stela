package fr.sictiam.stela.pesservice.service;

import fr.sictiam.stela.pesservice.dao.LocalAuthorityRepository;
import fr.sictiam.stela.pesservice.model.LocalAuthority;
import fr.sictiam.stela.pesservice.model.event.LocalAuthorityEvent;
import fr.sictiam.stela.pesservice.model.ui.GenericAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.io.IOException;
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

    public List<LocalAuthority> getAll() {
        return localAuthorityRepository.findAllByActiveTrueOrderByName();
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

    public Optional<LocalAuthority> getBySirenOrSirens(String siren) {
        return localAuthorityRepository.findByActiveTrueAndSirenEqualsOrSirens(siren, siren);
    }

    public boolean localAuthorityGranted(GenericAccount genericAccount, String siren) {

        return genericAccount.getLocalAuthorities().stream()
                .anyMatch(localAuthority -> localAuthority.getActivatedModules().contains("PES")
                        && localAuthority.getSiren().equals(siren));
    }

    @Transactional
    public void handleEvent(LocalAuthorityEvent event) throws IOException {
        LocalAuthority localAuthority = localAuthorityRepository.findByUuid(event.getUuid())
                .orElse(new LocalAuthority(event.getUuid(), event.getName(), event.getSiren()));

        if (event.getActivatedModules().contains("PES")) {
            localAuthority.setActive(true);
            // If the new LocalAuthority.siren is present in other LocalAuthority.sirens, we
            // need to remove it so the
            // siren is present only once in the activated local authorities (needed for the
            // PesRetour assignment)
            List<LocalAuthority> allLocalAuthorities = localAuthorityRepository
                    .findByActiveTrueAndSirens(localAuthority.getSiren());
            allLocalAuthorities.forEach(localAuth -> {
                localAuth.setSirens(localAuth.getSirens().stream()
                        .filter(siren -> !localAuthority.getSiren().equals(localAuth.getSiren()))
                        .collect(Collectors.toList()));
                createOrUpdate(localAuth);
            });
        }
        createOrUpdate(localAuthority);
    }

}

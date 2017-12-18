package fr.sictiam.stela.admin.service;

import java.util.List;
import java.util.Optional;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fr.sictiam.stela.admin.dao.LocalAuthorityRepository;
import fr.sictiam.stela.admin.model.LocalAuthority;
import fr.sictiam.stela.admin.model.Module;
import fr.sictiam.stela.admin.model.event.LocalAuthorityEvent;
import fr.sictiam.stela.admin.service.exceptions.NotFoundException;


@Service
public class LocalAuthorityService {

    private final LocalAuthorityRepository localAuthorityRepository;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Value("${application.amqp.admin.createdKey}")
    private String createdKey;

    @Value("${application.amqp.admin.exchange}")
    private String exchange;

    public LocalAuthorityService(LocalAuthorityRepository localAuthorityRepository) {
        this.localAuthorityRepository = localAuthorityRepository;
    }

    public LocalAuthority createOrUpdate(LocalAuthority localAuthority) {
        localAuthority = localAuthorityRepository.saveAndFlush(localAuthority);

        LocalAuthorityEvent localAutorityCreation = new LocalAuthorityEvent(localAuthority);
        
        amqpTemplate.convertAndSend(exchange, createdKey, localAutorityCreation);

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

    public List<LocalAuthority> getAll() {
        return localAuthorityRepository.findAll();
    }

    public LocalAuthority getByUuid(String uuid) {
        return localAuthorityRepository.findByUuid(uuid).orElseThrow(() -> new NotFoundException("notifications.admin.local_authority_not_found"));
    }

    public Optional<LocalAuthority> findByName(String name) {
        return localAuthorityRepository.findByName(name);
    }

    public Optional<LocalAuthority> findBySiren(String siren) {
        return localAuthorityRepository.findBySiren(siren);
    }
}

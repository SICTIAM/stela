package fr.sictiam.stela.acteservice.service;

import fr.sictiam.stela.acteservice.dao.LocalAuthorityRepository;
import fr.sictiam.stela.acteservice.model.LocalAuthority;
import fr.sictiam.stela.acteservice.service.util.NullAwareBeanUtilsBean;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@Service
public class LocalAuthorityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalAuthorityService.class);

    private final LocalAuthorityRepository localAuthorityRepository;

    @Autowired
    public LocalAuthorityService(LocalAuthorityRepository localAuthorityRepository) {
        this.localAuthorityRepository = localAuthorityRepository;
    }

    public LocalAuthority create(LocalAuthority localAuthority) {
        return localAuthorityRepository.save(localAuthority);
    }

    public LocalAuthority partialUpdate(LocalAuthority updatedLocalAuthority, String uuid) {
        LocalAuthority localAuthority = getByUuid(uuid);
        BeanUtilsBean notNull = new NullAwareBeanUtilsBean();
        try {
            notNull.copyProperties(localAuthority, updatedLocalAuthority);
        } catch (IllegalAccessException|InvocationTargetException e) {
            LOGGER.error("Error trying to copy properties: {}", e);
        }
        return localAuthorityRepository.save(localAuthority);
    }

    public List<LocalAuthority> getAll() {
        return localAuthorityRepository.findAll();
    }

    public LocalAuthority getByUuid(String uuid) {
        return localAuthorityRepository.findByUuid(uuid);
    }

}

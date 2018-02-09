package fr.sictiam.stela.pesservice.service;

import fr.sictiam.stela.pesservice.dao.PesRetourRepository;
import fr.sictiam.stela.pesservice.model.PesRetour;
import fr.sictiam.stela.pesservice.model.util.OffsetBasedPageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class PesRetourService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PesRetourService.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final PesRetourRepository pesRetourRepository;

    @Autowired
    public PesRetourService(PesRetourRepository pesRetourRepository) {
        this.pesRetourRepository = pesRetourRepository;
    }

    public PesRetour getByUuid(String uuid) {
        return pesRetourRepository.findByUuid(uuid).get();
    }

    public List<PesRetour> getAllByLocalAuthority(String currentLocalAuthorityUuid, Integer limit, Integer offset) {
        Pageable pageable = new OffsetBasedPageRequest(offset, limit);
        Page<PesRetour> page = pesRetourRepository.findAllByLocalAuthority_Uuid(currentLocalAuthorityUuid, pageable);
        return page.getContent();
    }

    public Long countAllByLocalAuthority(String currentLocalAuthorityUuid) {
        return pesRetourRepository.countAllByLocalAuthority_Uuid(currentLocalAuthorityUuid);
    }
}

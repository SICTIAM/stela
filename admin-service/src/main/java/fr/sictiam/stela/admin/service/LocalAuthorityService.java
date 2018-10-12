package fr.sictiam.stela.admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sictiam.stela.admin.dao.LocalAuthorityRepository;
import fr.sictiam.stela.admin.model.Certificate;
import fr.sictiam.stela.admin.model.LocalAuthority;
import fr.sictiam.stela.admin.model.Module;
import fr.sictiam.stela.admin.model.UI.Views;
import fr.sictiam.stela.admin.model.event.LocalAuthorityEvent;
import fr.sictiam.stela.admin.service.exceptions.NotFoundException;
import fr.sictiam.stela.admin.service.util.OffsetBasedPageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LocalAuthorityService {

    private final LocalAuthorityRepository localAuthorityRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalAuthorityService.class);

    @PersistenceContext
    private EntityManager entityManager;

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

        try {
            ObjectMapper mapper = new ObjectMapper();
            String body = mapper.writerWithView(Views.LocalAuthorityView.class)
                    .writeValueAsString(localAutorityCreation);
            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            Message amMessage = new Message(body.getBytes(), messageProperties);
            amqpTemplate.send(exchange, "", amMessage);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

        return localAuthority;
    }

    public LocalAuthority modify(LocalAuthority localAuthority) {
        return createOrUpdate(localAuthority);
    }

    public void addModule(String uuid, Module module) {
        LocalAuthority localAuthority = localAuthorityRepository.getOne(uuid);
        localAuthority.addModule(module);
        createOrUpdate(localAuthority);
    }

    public void removeModule(String uuid, Module module) {
        LocalAuthority localAuthority = localAuthorityRepository.getOne(uuid);
        localAuthority.removeModule(module);
        createOrUpdate(localAuthority);
    }

    public List<LocalAuthority> getAll() {
        return localAuthorityRepository.findAll();
    }

    public List<LocalAuthority> getAllWithPagination(Integer limit, Integer offset, String column,
            Sort.Direction direction) {
        Pageable pageable = new OffsetBasedPageRequest(offset, limit, new Sort(direction, column));
        Page page = localAuthorityRepository.findAll(pageable);
        return page.getContent();
    }

    public Long countAll() {
        return localAuthorityRepository.countAll();
    }

    public LocalAuthority getByUuid(String uuid) {
        return localAuthorityRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException("notifications.admin.local_authority_not_found"));
    }

    public Optional<LocalAuthority> getBySlugName(String slugName) {
        return localAuthorityRepository.findBySlugName(slugName);
    }

    public Optional<LocalAuthority> findByName(String name) {
        return localAuthorityRepository.findByName(name);
    }

    public Optional<LocalAuthority> findBySiren(String siren) {
        return localAuthorityRepository.findBySiren(siren);
    }

    public Optional<LocalAuthority> getByInstanceId(String instanceId) {
        return localAuthorityRepository.findByOzwilloInstanceInfo_InstanceId(instanceId);
    }

    public Optional<LocalAuthority> getByCertificate(String serial, String issuer) {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LocalAuthority> query = builder.createQuery(LocalAuthority.class);
        Root<LocalAuthority> localAuthorityRoot = query.from(LocalAuthority.class);
        Join<LocalAuthority, Certificate> localAuthorityCertificateJoin = localAuthorityRoot.join("certificates");

        query.select(builder.construct(LocalAuthority.class, localAuthorityRoot.get("uuid")));

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(builder.and(builder.equal(localAuthorityCertificateJoin.get("serial"), serial)));
        predicates.add(builder.and(builder.equal(localAuthorityCertificateJoin.get("issuer"), issuer)));
        query.where(predicates.toArray(new Predicate[predicates.size()]));

        List<LocalAuthority> results = entityManager.createQuery(query).setMaxResults(1).getResultList();
        return (results == null || results.isEmpty()) ? Optional.empty() : Optional.of(results.get(0));
    }
}

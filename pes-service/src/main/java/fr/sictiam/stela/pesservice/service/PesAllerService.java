package fr.sictiam.stela.pesservice.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import fr.sictiam.stela.pesservice.dao.AttachmentRepository;
import fr.sictiam.stela.pesservice.dao.PesHistoryRepository;
import fr.sictiam.stela.pesservice.dao.PesAllerRepository;
import fr.sictiam.stela.pesservice.model.PesAller;
import fr.sictiam.stela.pesservice.model.PesHistory;
import fr.sictiam.stela.pesservice.model.StatusType;
import fr.sictiam.stela.pesservice.model.event.PesHistoryEvent;
import fr.sictiam.stela.pesservice.service.exceptions.PesNotFoundException;

@Service
public class PesAllerService implements ApplicationListener<PesHistoryEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PesAllerService.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final PesAllerRepository pesAllerRepository;
    private final PesHistoryRepository pesHistoryRepository;
    private final AttachmentRepository attachmentRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final LocalAuthorityService localAuthorityService;

    @Autowired
    public PesAllerService(PesAllerRepository pesAllerRepository, PesHistoryRepository pesHistoryRepository,
            AttachmentRepository attachmentRepository, ApplicationEventPublisher applicationEventPublisher,
            LocalAuthorityService localAuthorityService) {
        this.pesAllerRepository = pesAllerRepository;
        this.pesHistoryRepository = pesHistoryRepository;
        this.attachmentRepository = attachmentRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.localAuthorityService = localAuthorityService;

    }
    
    public List<PesAller> getAllWithQuery(String number, String objet, LocalDate decisionFrom, LocalDate decisionTo,

            StatusType status) {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<PesAller> query = builder.createQuery(PesAller.class);
        Root<PesAller> pesRoot = query.from(PesAller.class);

        List<Predicate> predicates = new ArrayList<>();
        if (StringUtils.isNotBlank(objet))
            predicates.add(
                    builder.and(builder.like(builder.lower(pesRoot.get("objet")), "%" + objet.toLowerCase() + "%")));

        if (status != null) {
            // TODO: Find a way to do a self left join using a CriteriaQuery instead of a
            // native one
            Query q = entityManager.createNativeQuery(
                    "select ah1.pes_uuid from pes_history ah1 left join pes_history ah2 on (ah1.pes_uuid = ah2.pes_uuid and ah1.date < ah2.date) where ah2.date is null and ah1.status = '"
                            + status + "'");
            List<String> pesHistoriesActeUuids = q.getResultList();
            if (pesHistoriesActeUuids.size() > 0)
                predicates.add(builder.and(pesRoot.get("uuid").in(pesHistoriesActeUuids)));
            else
                predicates.add(builder.and(pesRoot.get("uuid").isNull()));
        }

        query.where(predicates.toArray(new Predicate[predicates.size()]))
                .orderBy(builder.desc(pesRoot.get("creation")));

        TypedQuery<PesAller> typedQuery = entityManager.createQuery(query);
        List<PesAller> pesList = typedQuery.getResultList();
        return pesList;
    }

    @Override
    public void onApplicationEvent(@NotNull PesHistoryEvent event) {
        PesAller pes = getByUuid(event.getPesHistory().getActeUuid());
        pes.getPesHistories().add(event.getPesHistory());
        pesAllerRepository.save(pes);
    }

    public PesAller getByUuid(String uuid) {
        return pesAllerRepository.findById(uuid).orElseThrow(PesNotFoundException::new);
    }

    public void updateStatus(String pesUuid, StatusType updatedStatus) {
        PesHistory pesHistory = new PesHistory(pesUuid, updatedStatus);
        applicationEventPublisher.publishEvent(new PesHistoryEvent(this, pesHistory));
    }

    public PesAller save(PesAller pes) {
        return pesAllerRepository.save(pes);
    }
}

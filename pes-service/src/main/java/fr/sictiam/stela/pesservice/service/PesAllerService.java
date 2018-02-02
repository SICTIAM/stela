package fr.sictiam.stela.pesservice.service;

import fr.sictiam.stela.pesservice.dao.PesAllerRepository;
import fr.sictiam.stela.pesservice.dao.PesHistoryRepository;
import fr.sictiam.stela.pesservice.model.*;
import fr.sictiam.stela.pesservice.model.event.PesHistoryEvent;
import fr.sictiam.stela.pesservice.service.exceptions.HistoryNotFoundException;
import fr.sictiam.stela.pesservice.service.exceptions.PesNotFoundException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class PesAllerService implements ApplicationListener<PesHistoryEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PesAllerService.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final PesAllerRepository pesAllerRepository;
    private final PesHistoryRepository pesHistoryRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final LocalAuthorityService localAuthorityService;

    @Autowired
    public PesAllerService(PesAllerRepository pesAllerRepository, PesHistoryRepository pesHistoryRepository,
                           ApplicationEventPublisher applicationEventPublisher, LocalAuthorityService localAuthorityService) {
        this.pesAllerRepository = pesAllerRepository;
        this.pesHistoryRepository = pesHistoryRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.localAuthorityService = localAuthorityService;

    }

    public Long countAllWithQuery(String objet, LocalDate creationFrom, LocalDate creationTo, StatusType status, String currentLocalAuthUuid) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<PesAller> pesRoot = query.from(PesAller.class);

        List<Predicate> predicates = getQueryPredicates(builder, pesRoot, objet, creationFrom, creationTo, status, currentLocalAuthUuid);
        query.select(builder.count(pesRoot));
        query.where(predicates.toArray(new Predicate[predicates.size()]));

        return entityManager.createQuery(query).getSingleResult();
    }

    public List<PesAller> getAllWithQuery(String objet, LocalDate creationFrom, LocalDate creationTo, StatusType status,
                                          Integer limit, Integer offset, String column, String direction, String currentLocalAuthUuid) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<PesAller> query = builder.createQuery(PesAller.class);
        Root<PesAller> pesRoot = query.from(PesAller.class);

        String columnAttribute = StringUtils.isEmpty(column) ? "creation" : column;
        List<Predicate> predicates = getQueryPredicates(builder, pesRoot, objet, creationFrom, creationTo, status, currentLocalAuthUuid);
        query.where(predicates.toArray(new Predicate[predicates.size()]))
                .orderBy(!StringUtils.isEmpty(direction) && direction.equals("ASC") ? builder.asc(pesRoot.get(columnAttribute)) : builder.desc(pesRoot.get(columnAttribute)));

        return entityManager.createQuery(query)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    private List<Predicate> getQueryPredicates(CriteriaBuilder builder, Root<PesAller> pesRoot, String objet, LocalDate creationFrom, LocalDate creationTo, StatusType status, String currentLocalAuthUuid) {
        List<Predicate> predicates = new ArrayList<>();
        if (StringUtils.isNotBlank(objet))
            predicates.add(builder.and(builder.like(builder.lower(pesRoot.get("objet")), "%" + objet.toLowerCase() + "%")));
        if (StringUtils.isNotBlank(currentLocalAuthUuid)) {
            Join<LocalAuthority, PesAller> LocalAuthorityJoin = pesRoot.join("localAuthority");
            LocalAuthorityJoin.on(builder.equal(LocalAuthorityJoin.get("uuid"), currentLocalAuthUuid));
        }

        if (creationFrom != null && creationTo != null)
            predicates.add(builder.and(builder.between(pesRoot.get("creation"), creationFrom.atStartOfDay(), creationTo.plusDays(1).atStartOfDay())));

        if (status != null) {
            // TODO: Find a way to do a self left join using a CriteriaQuery instead of a native one
            Query q = entityManager.createNativeQuery("select ah1.pes_uuid from pes_history ah1 left join pes_history ah2 on (ah1.pes_uuid = ah2.pes_uuid and ah1.date < ah2.date) where ah2.date is null and ah1.status = '" + status + "'");
            List<String> pesHistoriesPesUuids = q.getResultList();
            if (pesHistoriesPesUuids.size() > 0)
                predicates.add(builder.and(pesRoot.get("uuid").in(pesHistoriesPesUuids)));
            else predicates.add(builder.and(pesRoot.get("uuid").isNull()));
        }

        return predicates;
    }

    @Override
    public void onApplicationEvent(@NotNull PesHistoryEvent event) {
        PesAller pes = getByUuid(event.getPesHistory().getPesUuid());
        pes.getPesHistories().add(event.getPesHistory());
        pesAllerRepository.save(pes);
    }

    public PesAller create(String currentProfileUuid, String currentLocalAuthUuid, PesAller pesAller, MultipartFile file) throws IOException {
        pesAller.setLocalAuthority(localAuthorityService.getByUuid(currentLocalAuthUuid));
        pesAller.setProfileUuid(currentProfileUuid);
        Attachment attachment = new Attachment(file.getBytes(), file.getOriginalFilename(), file.getSize());
        pesAller.setAttachment(attachment);
        pesAller.setCreation(LocalDateTime.now());

        pesAller = pesAllerRepository.save(pesAller);
        updateStatus(pesAller.getUuid(), StatusType.CREATED);
        return pesAller;
    }

    public PesAller getByUuid(String uuid) {
        return pesAllerRepository.findById(uuid).orElseThrow(PesNotFoundException::new);
    }

    public void updateStatus(String pesUuid, StatusType updatedStatus) {
        PesHistory pesHistory = new PesHistory(pesUuid, updatedStatus);
        applicationEventPublisher.publishEvent(new PesHistoryEvent(this, pesHistory));
    }
    
    public void updateStatus(String pesUuid, StatusType updatedStatus, String messsage) {
        PesHistory pesHistory = new PesHistory(pesUuid, updatedStatus, LocalDateTime.now(), messsage);
        applicationEventPublisher.publishEvent(new PesHistoryEvent(this, pesHistory));
    }
    
    public void updateStatus(String pesUuid, StatusType updatedStatus, byte [] file ,String fileName) {
        PesHistory pesHistory = new PesHistory(pesUuid, updatedStatus, LocalDateTime.now(), file, fileName);
        applicationEventPublisher.publishEvent(new PesHistoryEvent(this, pesHistory));
    }

    public PesAller save(PesAller pes) {
        return pesAllerRepository.save(pes);
    }

    public PesAller getByAttachementName(String fileName) {
        return pesAllerRepository.findByAttachment_filename(fileName).orElseThrow(PesNotFoundException::new);
    }

    public List<PesAller> getBlockedFlux() {
        return pesAllerRepository.findAllByPesHistories_statusAndPesHistories_statusNotIn(StatusType.SENT, Arrays.asList(StatusType.ACK_RECEIVED, StatusType.MAX_RETRY_REACH));
    }
    
    public PesHistory getLastSentHistory(String uuid) {
        return pesHistoryRepository.findBypesUuidAndStatusInOrderByDateDesc(uuid, Arrays.asList(StatusType.SENT, StatusType.RESENT)).get(0);        
    }

    public PesHistory getHistoryByUuid(String uuid) {
        return pesHistoryRepository.findByUuid(uuid).orElseThrow(HistoryNotFoundException::new);
    }
}

package fr.sictiam.stela.acteservice.service;

import fr.sictiam.stela.acteservice.dao.AttachmentRepository;
import fr.sictiam.stela.acteservice.model.*;
import fr.sictiam.stela.acteservice.model.ui.ActeSearchUI;
import fr.sictiam.stela.acteservice.service.exceptions.ActeNotFoundException;
import fr.sictiam.stela.acteservice.service.exceptions.CancelForbiddenException;
import fr.sictiam.stela.acteservice.service.exceptions.FileNotFoundException;
import fr.sictiam.stela.acteservice.service.exceptions.HistoryNotFoundException;
import fr.sictiam.stela.acteservice.dao.ActeHistoryRepository;
import fr.sictiam.stela.acteservice.dao.ActeRepository;
import fr.sictiam.stela.acteservice.model.event.ActeHistoryEvent;
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
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ActeService implements ApplicationListener<ActeHistoryEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActeService.class);

    @PersistenceContext
    private EntityManager entityManager;
    
    private final ActeRepository acteRepository;
    private final ActeHistoryRepository acteHistoryRepository;
    private final AttachmentRepository attachmentRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public ActeService(ActeRepository acteRepository, ActeHistoryRepository acteHistoryRepository, AttachmentRepository attachmentRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.acteRepository = acteRepository;
        this.acteHistoryRepository = acteHistoryRepository;
        this.attachmentRepository = attachmentRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * Create new Acte entity in databaseFilename, compress the files to a tar.gz archive and delivers it to minister.
     * 
     * @param acte Acte's data used to create Acte entity.
     * @param file Acte's file.
     * @param annexes Acte's annexes.
     * 
     * @return The newly created Acte entity.
     */
    public Acte create(LocalAuthority currentLocalAuthority, Acte acte, MultipartFile file, MultipartFile... annexes)
            throws ActeNotSentException, IOException {
        acte.setFilename(file.getOriginalFilename());
        acte.setFile(file.getBytes());
        List<Attachment> transformedAnnexes = new ArrayList<>();
        for (MultipartFile annexe: annexes) {
            transformedAnnexes.add(new Attachment(annexe.getBytes(), annexe.getOriginalFilename()));
        }
        acte.setAnnexes(transformedAnnexes);
        acte.setCreation(LocalDateTime.now());

        if(!currentLocalAuthority.getCanPublishWebSite()) acte.setPublicWebsite(false);
        if(!currentLocalAuthority.getCanPublishRegistre()) acte.setPublic(false);

        Acte created = acteRepository.save(acte);

        ActeHistory acteHistory = new ActeHistory(acte.getUuid(), StatusType.CREATED);
        applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory));

        LOGGER.info("Acte {} created with id {}", created.getNumber(), created.getUuid());

        return created;
    }

    public List<Acte> getAll() {
        return acteRepository.findAllByOrderByCreationDesc();
    }

    public List<Acte> getAllWithQuery(String number, String title, ActeNature nature, LocalDate decisionFrom, LocalDate decisionTo, StatusType status) {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Acte> query = builder.createQuery(Acte.class);
        Root<Acte> acteRoot = query.from(Acte.class);

        List<Predicate> predicates = new ArrayList<>();
        if(StringUtils.isNotBlank(number)) predicates.add(builder.and(builder.like(acteRoot.get("number"), "%"+number+"%")));
        if(StringUtils.isNotBlank(title)) predicates.add(builder.and(builder.like(acteRoot.get("title"), "%"+title+"%")));
        if(nature != null) predicates.add(builder.and(builder.equal(acteRoot.get("nature"), nature)));
        if(decisionFrom != null && decisionTo != null) predicates.add(builder.and(builder.between(acteRoot.get("decision"), decisionFrom, decisionTo)));

        if(status != null) {
            // TODO: Find a way to do a self left join using a CriteriaQuery instead of a native one
            Query q = entityManager.createNativeQuery("select ah1.acte_uuid from acte_history ah1 left join acte_history ah2 on (ah1.acte_uuid = ah2.acte_uuid and ah1.date < ah2.date) where ah2.date is null and ah1.status = '" + status + "'");
            List<String> acteHistoriesActeUuids = q.getResultList();
            if(acteHistoriesActeUuids.size() > 0) predicates.add(builder.and(acteRoot.get("uuid").in(acteHistoriesActeUuids)));
            else predicates.add(builder.and(acteRoot.get("uuid").isNull()));
        }

        query.where(predicates.toArray(new Predicate[predicates.size()]))
            .orderBy(builder.desc(acteRoot.get("creation")));

        TypedQuery<Acte> typedQuery = entityManager.createQuery(query);
        List<Acte> actes = typedQuery.getResultList();
        return actes;
    }

    public Acte getByUuid(String uuid) {
        return acteRepository.findByUuid(uuid).orElseThrow(ActeNotFoundException::new);
    }

    public List<Attachment> getAnnexes(String acteUuid) {
        return getByUuid(acteUuid).getAnnexes();
    }

    public Attachment getAnnexeByUuid(String uuid) {
        return attachmentRepository.findByUuid(uuid).orElseThrow(FileNotFoundException::new);
    }

    public ActeHistory getHistoryByUuid(String uuid) {
        return acteHistoryRepository.findByUuid(uuid).orElseThrow(HistoryNotFoundException::new);
    }

    public void cancel(String uuid) {
        if(isCancellable(uuid)) {
            ActeHistory acteHistory = new ActeHistory(uuid, StatusType.CANCELLATION_ASKED);
            applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory));
        } else throw new CancelForbiddenException();
    }

    public boolean isCancellable(String uuid) {
        // TODO: Improve later when phases will be supported
        Acte acte = getByUuid(uuid);
        List<StatusType> cancelPendingStatus = Arrays.asList(StatusType.CANCELLATION_ASKED, StatusType.CANCELLATION_ARCHIVE_CREATED, StatusType.ARCHIVE_SIZE_CHECKED);
        SortedSet<ActeHistory> acteHistoryList = acte.getActeHistories();
        return acteHistoryList.stream().anyMatch(acteHistory -> acteHistory.getStatus().equals(StatusType.ACK_RECEIVED))
                && acteHistoryList.stream().noneMatch(acteHistory -> acteHistory.getStatus().equals(StatusType.CANCELLED))
                && !cancelPendingStatus.contains(acte.getActeHistories().last().getStatus());
    }

    @Override
    public void onApplicationEvent(@NotNull ActeHistoryEvent event) {
        Acte acte = getByUuid(event.getActeHistory().getActeUuid());
        SortedSet<ActeHistory> acteHistories = acte.getActeHistories();
        acteHistories.add(event.getActeHistory());
        acte.setActeHistories(acteHistories);
        acteRepository.save(acte);
    }
}

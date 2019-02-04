package fr.sictiam.stela.convocationservice.service;

import fr.sictiam.stela.convocationservice.dao.ConvocationRepository;
import fr.sictiam.stela.convocationservice.dao.RecipientResponseRepository;
import fr.sictiam.stela.convocationservice.model.Convocation;
import fr.sictiam.stela.convocationservice.model.LocalAuthority;
import fr.sictiam.stela.convocationservice.model.Recipient;
import fr.sictiam.stela.convocationservice.model.RecipientResponse;
import fr.sictiam.stela.convocationservice.model.exception.ConvocationException;
import fr.sictiam.stela.convocationservice.model.exception.NotFoundException;
import fr.sictiam.stela.convocationservice.model.util.ConvocationBeanUtils;
import fr.sictiam.stela.convocationservice.service.exceptions.ConvocationNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConvocationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConvocationService.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final ConvocationRepository convocationRepository;

    private final LocalAuthorityService localAuthorityService;

    private final RecipientResponseRepository recipientResponseRepository;

    @Autowired
    public ConvocationService(
            ConvocationRepository convocationRepository,
            LocalAuthorityService localAuthorityService,
            RecipientResponseRepository recipientResponseRepository) {
        this.convocationRepository = convocationRepository;
        this.localAuthorityService = localAuthorityService;
        this.recipientResponseRepository = recipientResponseRepository;
    }

    public Convocation getConvocation(String uuid) {
        return convocationRepository.findById(uuid).orElseThrow(() -> new NotFoundException("Convocation " + uuid + " not found"));
    }

    public Convocation openBy(Convocation convocation, Recipient recipient) {

        RecipientResponse response =
                convocation.getRecipientResponses()
                        .stream().filter(recipientResponse -> recipientResponse.getRecipient().getUuid().equals(recipient.getUuid())).findFirst().get();
        if (!response.isOpened()) {
            response.setOpened(true);
            response.setOpenDate(LocalDateTime.now());
            recipientResponseRepository.save(response);
        }
        return convocation;
    }

    public Convocation create(Convocation convocation, String localAuthorityUuid, String profileUuid) {

        LocalAuthority localAuthority = localAuthorityService.getLocalAuthority(localAuthorityUuid);

        if (convocation.getMeetingDate().isBefore(LocalDateTime.now())) {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LOGGER.error("Invalid meeting date: {}", formatter.format(convocation.getMeetingDate()));
            throw new ConvocationException("convocation.errors.convocation.invalidDate");
        }
        convocation.setCreationDate(LocalDateTime.now());
        convocation.setProfileUuid(profileUuid);
        convocation.setLocalAuthority(localAuthority);

        // TEMPORARY : changed when mail will be sent
        convocation.setSentDate(LocalDateTime.now());

        convocation = convocationRepository.saveAndFlush(convocation);

        createRecipientResponse(convocation);

        return convocationRepository.save(convocation);
    }

    private void createRecipientResponse(Convocation convocation) {
        convocation.setRecipientResponses(
                convocation.getRecipients().stream().map(recipient -> {
                    RecipientResponse recipientResponse = new RecipientResponse(recipient);
                    recipientResponse.setConvocation(convocation);
                    recipientResponseRepository.save(recipientResponse);
                    return recipientResponse;
                }).collect(Collectors.toSet()));
    }

    public Convocation update(String uuid, String localAuthorityUuid, Convocation params) {
        Convocation convocation = getByUuid(uuid, localAuthorityUuid);
        ConvocationBeanUtils.mergeProperties(params, convocation, "uuid", "creationDate");

        return convocationRepository.saveAndFlush(convocation);
    }

    public Convocation getConvocation(String uuid, String localAuthorityUuid) {

        return convocationRepository
                .findByUuidAndLocalAuthorityUuid(uuid, localAuthorityUuid)
                .orElseThrow(() -> new NotFoundException("Convocation " + uuid + " not found in local authority " + localAuthorityUuid));
    }

    public void delete(Convocation convocation) {
        convocationRepository.delete(convocation);
    }

    public Convocation getByUuid(String uuid, String localAuthorityUuid) {
        return convocationRepository.findByUuidAndLocalAuthorityUuid(uuid, localAuthorityUuid).orElseThrow(ConvocationNotFoundException::new);
    }

    public Long countAllWithQuery(String multifield, LocalDate sentDateFrom, LocalDate sentDateTo, String assemblyType,
            LocalDate meetingDateFrom, LocalDate meetingDateTo, String subject, Integer limit, Integer offset, String column, String direction, String currentLocalAuthUuid) {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Convocation> convocationRoot = query.from(Convocation.class);

        List<Predicate> predicates = getQueryPredicates(builder, convocationRoot, multifield, sentDateFrom, sentDateTo, assemblyType,
                meetingDateFrom, meetingDateTo, subject, currentLocalAuthUuid);
        query.select(builder.count(convocationRoot));
        query.where(predicates.toArray(new Predicate[predicates.size()]));

        return entityManager.createQuery(query).getSingleResult();
    }

    public List<Convocation> findAllWithQuery(String multifield, LocalDate sentDateFrom, LocalDate sentDateTo, String assemblyType,
            LocalDate meetingDateFrom, LocalDate meetingDateTo, String subject, Integer limit, Integer offset, String column, String direction,
            String currentLocalAuthUuid) {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Convocation> query = builder.createQuery(Convocation.class);
        Root<Convocation> convocationRoot = query.from(Convocation.class);

        query.select(convocationRoot);
        String columnAttribute = StringUtils.isEmpty(column) ? "meetingDate" : column;
        List<Predicate> predicates = getQueryPredicates(builder, convocationRoot, multifield, sentDateFrom, sentDateTo, assemblyType,
                meetingDateFrom, meetingDateTo, subject, currentLocalAuthUuid);

        query.where(predicates.toArray(new Predicate[predicates.size()]))
                .orderBy(!StringUtils.isEmpty(direction) && direction.equals("ASC")
                        ? builder.asc(convocationRoot.get(columnAttribute))
                        : builder.desc(convocationRoot.get(columnAttribute)));
        return entityManager.createQuery(query).setFirstResult(offset).setMaxResults(limit).getResultList();
    }

    private List<Predicate> getQueryPredicates(CriteriaBuilder builder, Root<Convocation> convocationRoot,
            String multifield, LocalDate sentDateFrom, LocalDate sentDateTo, String assemblyType, LocalDate meetingDateFrom, LocalDate meetingDateTo, String subject, String currentLocalAuthUuid) {

        List<Predicate> predicates = new ArrayList<>();
        if (StringUtils.isNotBlank(multifield)) {
            List<Predicate> multifieldPredicates = new ArrayList<>();

            LocalDate date = null;
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                date = LocalDate.parse(multifield, formatter);
            } catch (DateTimeParseException e) {
                // Nothing to do, multifield is not a valid date
            }

            if (date != null) {
                multifieldPredicates.add(builder.between(convocationRoot.get("sentDate"), date.atStartOfDay(),
                        date.plusDays(1).atStartOfDay()));
                multifieldPredicates.add(builder.between(convocationRoot.get("meetingDate"), date.atStartOfDay(),
                        date.plusDays(1).atStartOfDay()));
            }

            multifieldPredicates.add(builder.like(builder.lower(convocationRoot.get("subject")),
                    "%" + multifield.toLowerCase() + "%"));
            multifieldPredicates.add(builder.like(builder.lower(convocationRoot.get("assemblyType").get("name")),
                    "%" + multifield.toLowerCase() + "%"));

            predicates.add(builder.or(multifieldPredicates.toArray(new Predicate[]{})));
        }

        if (StringUtils.isNotBlank(assemblyType))
            predicates.add(
                    builder.and(builder.like(builder.lower(convocationRoot.get("assemblyType").get("name")),
                            "%" + assemblyType.toLowerCase() + "%")));

        if (StringUtils.isNotBlank(subject))
            predicates.add(
                    builder.and(builder.like(builder.lower(convocationRoot.get("subject")),
                            "%" + subject.toLowerCase() + "%")));

        if (sentDateFrom != null && sentDateTo != null)
            predicates.add(
                    builder.and(builder.between(convocationRoot.get("sentDate"), sentDateFrom.atStartOfDay(),
                            sentDateTo.plusDays(1).atStartOfDay())));

        if (meetingDateFrom != null && meetingDateTo != null)
            predicates.add(
                    builder.and(builder.between(convocationRoot.get("meetingDate"), meetingDateFrom.atStartOfDay(),
                            meetingDateTo.plusDays(1).atStartOfDay())));

        if (StringUtils.isNotBlank(currentLocalAuthUuid)) {
            Join<LocalAuthority, Convocation> LocalAuthorityJoin = convocationRoot.join("localAuthority");
            LocalAuthorityJoin.on(builder.equal(LocalAuthorityJoin.get("uuid"), currentLocalAuthUuid));
        }

        return predicates;
    }
}

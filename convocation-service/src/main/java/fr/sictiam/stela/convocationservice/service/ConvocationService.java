package fr.sictiam.stela.convocationservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import fr.sictiam.stela.convocationservice.dao.AttachmentRepository;
import fr.sictiam.stela.convocationservice.dao.ConvocationRepository;
import fr.sictiam.stela.convocationservice.dao.RecipientResponseRepository;
import fr.sictiam.stela.convocationservice.model.Attachment;
import fr.sictiam.stela.convocationservice.model.Convocation;
import fr.sictiam.stela.convocationservice.model.LocalAuthority;
import fr.sictiam.stela.convocationservice.model.Profile;
import fr.sictiam.stela.convocationservice.model.Recipient;
import fr.sictiam.stela.convocationservice.model.RecipientResponse;
import fr.sictiam.stela.convocationservice.model.ResponseType;
import fr.sictiam.stela.convocationservice.model.event.FileUploadEvent;
import fr.sictiam.stela.convocationservice.model.exception.ConvocationException;
import fr.sictiam.stela.convocationservice.model.exception.ConvocationFileException;
import fr.sictiam.stela.convocationservice.model.exception.NotFoundException;
import fr.sictiam.stela.convocationservice.model.util.ConvocationBeanUtils;
import fr.sictiam.stela.convocationservice.service.exceptions.ConvocationNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ConvocationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConvocationService.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final ConvocationRepository convocationRepository;

    private final LocalAuthorityService localAuthorityService;

    private final StorageService storageService;

    private final ExternalRestService externalRestService;

    private final RecipientResponseRepository recipientResponseRepository;

    private final AttachmentRepository attachmentRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public ConvocationService(
            ConvocationRepository convocationRepository,
            LocalAuthorityService localAuthorityService,
            StorageService storageService,
            ExternalRestService externalRestService,
            RecipientResponseRepository recipientResponseRepository,
            AttachmentRepository attachmentRepository,
            ApplicationEventPublisher applicationEventPublisher) {
        this.convocationRepository = convocationRepository;
        this.localAuthorityService = localAuthorityService;
        this.storageService = storageService;
        this.externalRestService = externalRestService;
        this.recipientResponseRepository = recipientResponseRepository;
        this.attachmentRepository = attachmentRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public Convocation getConvocation(String uuid) {
        return convocationRepository.findById(uuid).orElseThrow(() -> new NotFoundException("Convocation " + uuid + " not found"));
    }

    public Convocation openBy(Convocation convocation, Recipient recipient) {

        JsonNode jsonProfile = externalRestService.getProfile(convocation.getProfileUuid());
        if (jsonProfile != null) {
            convocation.setProfile(new Profile(
                    jsonProfile.get("uuid").asText(""),
                    jsonProfile.get("agent").get("given_name").asText(""),
                    jsonProfile.get("agent").get("family_name").asText(""),
                    jsonProfile.get("agent").get("email").asText("")));
        }

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
        convocation.getRecipientResponses().addAll(
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

    public void uploadFiles(Convocation convocation, MultipartFile file, MultipartFile... annexes)
            throws ConvocationFileException {

        if (convocation.getAttachment() != null && file != null) {
            LOGGER.error("Try to overwrite main document for convocation {} ({})", convocation.getSubject(),
                    convocation.getUuid());
            throw new ConvocationFileException("convocation.errors.convocation.fileExists");
        } else if (convocation.getAttachment() == null && file == null) {
            LOGGER.error("Main document missing for convocation {} ({})", convocation.getSubject(),
                    convocation.getUuid());
            throw new ConvocationFileException("convocation.errors.convocation.documentMissing");
        }

        if (file != null) {
            Attachment attachment = saveAttachment(file);
            convocation.setAttachment(attachment);
        }

        for (MultipartFile annexe : annexes) {
            Attachment attachment = saveAttachment(annexe);
            convocation.getAnnexes().add(attachment);
        }

        convocationRepository.save(convocation);
    }

    public Attachment getFile(String currentLocalAuthUuid, String uuid, String fileUuid) throws NotFoundException {

        Convocation convocation = getConvocation(uuid, currentLocalAuthUuid);
        if (convocation.getAttachment().getUuid().equals(fileUuid)) {
            storageService.getAttachmentContent(convocation.getAttachment());
            return convocation.getAttachment();
        }

        for (Attachment annexe : convocation.getAnnexes()) {
            if (annexe.getUuid().equals(fileUuid)) {
                storageService.getAttachmentContent(annexe);
                return annexe;
            }
        }

        throw new NotFoundException("file not found");
    }

    public void answer(Convocation convocation, Recipient recipient, ResponseType responseType) {

        Optional<RecipientResponse> recipientResponse =
                convocation.getRecipientResponses().stream().filter(rr -> rr.getRecipient().equals(recipient)).findFirst();
        if (!recipientResponse.isPresent()) {
            LOGGER.error("Recipient {} not found in convocation {}", recipient.getUuid(), convocation.getUuid());
            throw new NotFoundException("Recipient " + recipient.getUuid() + " not found in convocation " + convocation.getUuid());
        }

        recipientResponse.get().setResponseType(responseType);
        convocationRepository.save(convocation);
    }

    public Long countSentWithQuery(String multifield, LocalDate sentDateFrom, LocalDate sentDateTo, String assemblyType,
            LocalDate meetingDateFrom, LocalDate meetingDateTo, String subject, String currentLocalAuthUuid) {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Convocation> convocationRoot = query.from(Convocation.class);

        List<Predicate> predicates = getSentQueryPredicates(builder, convocationRoot, multifield, sentDateFrom,
                sentDateTo, assemblyType,
                meetingDateFrom, meetingDateTo, subject, currentLocalAuthUuid);
        query.select(builder.count(convocationRoot));
        query.where(predicates.toArray(new Predicate[predicates.size()]));

        return entityManager.createQuery(query).getSingleResult();
    }

    public Long countReceivedWithQuery(String multifield, String assemblyType, LocalDate meetingDateFrom,
            LocalDate meetingDateTo, String subject, String filter, String currentLocalAuthUuid, Recipient recipient) {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Convocation> convocationRoot = query.from(Convocation.class);

        List<Predicate> predicates = getReceivedQueryPredicates(builder, convocationRoot, multifield, assemblyType,
                meetingDateFrom, meetingDateTo, subject, filter, currentLocalAuthUuid, recipient);
        query.select(builder.count(convocationRoot));
        query.where(predicates.toArray(new Predicate[predicates.size()]));

        return entityManager.createQuery(query).getSingleResult();
    }

    public List<Convocation> findSentWithQuery(String multifield, LocalDate sentDateFrom, LocalDate sentDateTo,
            String assemblyType,
            LocalDate meetingDateFrom, LocalDate meetingDateTo, String subject, Integer limit, Integer offset, String column, String direction,
            String currentLocalAuthUuid) {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Convocation> query = builder.createQuery(Convocation.class);
        Root<Convocation> convocationRoot = query.from(Convocation.class);

        query.select(convocationRoot);
        String columnAttribute = StringUtils.isEmpty(column) ? "meetingDate" : column;
        List<Predicate> predicates = getSentQueryPredicates(builder, convocationRoot, multifield, sentDateFrom,
                sentDateTo, assemblyType,
                meetingDateFrom, meetingDateTo, subject, currentLocalAuthUuid);

        query.where(predicates.toArray(new Predicate[predicates.size()]))
                .orderBy(!StringUtils.isEmpty(direction) && direction.equals("ASC")
                        ? builder.asc(convocationRoot.get(columnAttribute))
                        : builder.desc(convocationRoot.get(columnAttribute)));
        return entityManager.createQuery(query).setFirstResult(offset).setMaxResults(limit).getResultList();
    }

    public List<Convocation> findReceivedWithQuery(String multifield, String assemblyType,
            LocalDate meetingDateFrom, LocalDate meetingDateTo, String subject, String filter, Integer limit,
            Integer offset, String column, String direction,
            String currentLocalAuthUuid, Recipient recipient) {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Convocation> query = builder.createQuery(Convocation.class);
        Root<Convocation> convocationRoot = query.from(Convocation.class);

        query.select(convocationRoot);
        String columnAttribute = StringUtils.isEmpty(column) ? "meetingDate" : column;
        List<Predicate> predicates = getReceivedQueryPredicates(builder, convocationRoot, multifield, assemblyType,
                meetingDateFrom, meetingDateTo, subject, filter, currentLocalAuthUuid, recipient);

        query.where(predicates.toArray(new Predicate[predicates.size()]))
                .orderBy(!StringUtils.isEmpty(direction) && direction.equals("ASC")
                        ? builder.asc(convocationRoot.get(columnAttribute))
                        : builder.desc(convocationRoot.get(columnAttribute)));
        return entityManager.createQuery(query).setFirstResult(offset).setMaxResults(limit).getResultList();
    }

    private List<Predicate> getSentQueryPredicates(CriteriaBuilder builder, Root<Convocation> convocationRoot,
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

        if (sentDateFrom != null)
            predicates.add(
                    builder.and(builder.greaterThanOrEqualTo(convocationRoot.get("sentDate"),
                            sentDateFrom.atStartOfDay())));
        if (sentDateTo != null)
            predicates.add(
                    builder.and(builder.lessThan(convocationRoot.get("sentDate"),
                            sentDateTo.plusDays(1).atStartOfDay())));

        if (meetingDateFrom != null)
            predicates.add(
                    builder.and(builder.greaterThanOrEqualTo(convocationRoot.get("meetingDate"),
                            meetingDateFrom.atStartOfDay())));
        if (meetingDateTo != null)
            predicates.add(
                    builder.and(builder.lessThan(convocationRoot.get("meetingDate"),
                            meetingDateTo.plusDays(1).atStartOfDay())));

        if (StringUtils.isNotBlank(currentLocalAuthUuid)) {
            Join<LocalAuthority, Convocation> LocalAuthorityJoin = convocationRoot.join("localAuthority");
            LocalAuthorityJoin.on(builder.equal(LocalAuthorityJoin.get("uuid"), currentLocalAuthUuid));
        }

        return predicates;
    }

    private Attachment saveAttachment(MultipartFile file) throws ConvocationFileException {

        try {
            Attachment attachment = new Attachment(file.getOriginalFilename(), file.getBytes());
            attachment = attachmentRepository.save(attachment);
            applicationEventPublisher.publishEvent(new FileUploadEvent(this, attachment));
            return attachment;
        } catch (IOException e) {
            throw new ConvocationFileException("Failed to get bytes from file " + file.getOriginalFilename(), e);
        }
    }

    private List<Predicate> getReceivedQueryPredicates(CriteriaBuilder builder, Root<Convocation> convocationRoot,
            String multifield, String assemblyType, LocalDate meetingDateFrom, LocalDate meetingDateTo, String subject,
            String filter, String currentLocalAuthUuid, Recipient recipient) {

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
                    builder.and(builder.equal(convocationRoot.get("assemblyType").get("uuid"), assemblyType)));

        if (StringUtils.isNotBlank(subject))
            predicates.add(
                    builder.and(builder.like(builder.lower(convocationRoot.get("subject")),
                            "%" + subject.toLowerCase() + "%")));

        if (meetingDateFrom != null)
            predicates.add(
                    builder.and(builder.greaterThanOrEqualTo(convocationRoot.get("meetingDate"),
                            meetingDateFrom.atStartOfDay())));
        if (meetingDateTo != null)
            predicates.add(
                    builder.and(builder.lessThan(convocationRoot.get("meetingDate"),
                            meetingDateTo.plusDays(1).atStartOfDay())));

        if (meetingDateFrom == null && meetingDateTo == null && StringUtils.isNotBlank(filter)) {
            if (filter.equals("past"))
                predicates.add(
                        builder.and(builder.lessThan(convocationRoot.get("meetingDate"), LocalDateTime.now())));
            else if (filter.equals("future"))
                predicates.add(
                        builder.and(builder.greaterThan(convocationRoot.get("meetingDate"), LocalDateTime.now())));
        }

        if (StringUtils.isNotBlank(currentLocalAuthUuid)) {
            Join<LocalAuthority, Convocation> localAuthorityJoin = convocationRoot.join("localAuthority");
            localAuthorityJoin.on(builder.equal(localAuthorityJoin.get("uuid"), currentLocalAuthUuid));
        }

        Join<RecipientResponse, Convocation> recipientResponseJoin = convocationRoot.join("recipientResponses");
        recipientResponseJoin.on(builder.equal(recipientResponseJoin.get("recipient"), recipient));

        return predicates;
    }
}

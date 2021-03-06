package fr.sictiam.stela.convocationservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import fr.sictiam.stela.convocationservice.dao.AttachmentRepository;
import fr.sictiam.stela.convocationservice.dao.ConvocationRepository;
import fr.sictiam.stela.convocationservice.dao.QuestionResponseRepository;
import fr.sictiam.stela.convocationservice.dao.RecipientResponseRepository;
import fr.sictiam.stela.convocationservice.model.*;
import fr.sictiam.stela.convocationservice.model.event.FileUploadEvent;
import fr.sictiam.stela.convocationservice.model.event.HistoryEvent;
import fr.sictiam.stela.convocationservice.model.event.notifications.ConvocationCancelledEvent;
import fr.sictiam.stela.convocationservice.model.event.notifications.ConvocationCreatedEvent;
import fr.sictiam.stela.convocationservice.model.event.notifications.ConvocationReadEvent;
import fr.sictiam.stela.convocationservice.model.event.notifications.ConvocationRecipientAddedEvent;
import fr.sictiam.stela.convocationservice.model.event.notifications.ConvocationResponseEvent;
import fr.sictiam.stela.convocationservice.model.event.notifications.ConvocationUpdatedEvent;
import fr.sictiam.stela.convocationservice.model.event.notifications.MinutesAddedEvent;
import fr.sictiam.stela.convocationservice.model.event.notifications.ProcurationCancelledEvent;
import fr.sictiam.stela.convocationservice.model.event.notifications.ProcurationReceivedEvent;
import fr.sictiam.stela.convocationservice.model.exception.ConvocationCancelledException;
import fr.sictiam.stela.convocationservice.model.exception.ConvocationException;
import fr.sictiam.stela.convocationservice.model.exception.ConvocationFileException;
import fr.sictiam.stela.convocationservice.model.exception.ConvocationNotAvailableException;
import fr.sictiam.stela.convocationservice.model.exception.MissingParameterException;
import fr.sictiam.stela.convocationservice.model.exception.NotFoundException;
import fr.sictiam.stela.convocationservice.model.exception.ProcurationNotPermittedException;
import fr.sictiam.stela.convocationservice.service.exceptions.ConvocationNotFoundException;
import fr.sictiam.stela.convocationservice.service.util.PdfGeneratorUtil;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

    private final LocalesService localesService;

    private final TagService tagService;

    private final RecipientResponseRepository recipientResponseRepository;

    private final QuestionResponseRepository questionResponseRepository;

    private final AttachmentRepository attachmentRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final PdfGeneratorUtil pdfGeneratorUtil;

    @Autowired
    public ConvocationService(
            ConvocationRepository convocationRepository,
            LocalAuthorityService localAuthorityService,
            StorageService storageService,
            ExternalRestService externalRestService,
            LocalesService localesService,
            TagService tagService,
            RecipientResponseRepository recipientResponseRepository,
            QuestionResponseRepository questionResponseRepository,
            AttachmentRepository attachmentRepository,
            ApplicationEventPublisher applicationEventPublisher,
            PdfGeneratorUtil pdfGeneratorUtil) {
        this.convocationRepository = convocationRepository;
        this.localAuthorityService = localAuthorityService;
        this.storageService = storageService;
        this.externalRestService = externalRestService;
        this.localesService = localesService;
        this.tagService = tagService;
        this.recipientResponseRepository = recipientResponseRepository;
        this.questionResponseRepository = questionResponseRepository;
        this.attachmentRepository = attachmentRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.pdfGeneratorUtil = pdfGeneratorUtil;
    }

    public Convocation getConvocation(String uuid) {
        return convocationRepository.findById(uuid).orElseThrow(() -> new NotFoundException("Convocation " + uuid + " not found"));
    }

    public Convocation openBy(Convocation convocation, Recipient recipient) {

        convocation.setProfile(retrieveProfile(convocation.getProfileUuid()));

        RecipientResponse response =
                convocation.getRecipientResponses()
                        .stream().filter(recipientResponse -> recipientResponse.getRecipient().getUuid().equals(recipient.getUuid())).findFirst().get();
        if (!response.isOpened()) {
            response.setOpened(true);
            response.setOpenDate(LocalDateTime.now());
            recipientResponseRepository.save(response);
            applicationEventPublisher.publishEvent(new ConvocationReadEvent(this, convocation, response));
            addHistory(convocation, HistoryType.CONVOCATION_READ, recipient.getFullName(), false);
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

        convocation = convocationRepository.saveAndFlush(convocation);

        createRecipientResponse(convocation);

        convocation = convocationRepository.save(convocation);
        addHistory(convocation, HistoryType.CREATED);
        applicationEventPublisher.publishEvent(new ConvocationCreatedEvent(this, convocation));
        return convocation;
    }

    private void createRecipientResponse(Convocation convocation) {
        convocation.getRecipientResponses().addAll(
                convocation.getRecipients().stream().map(recipient -> {
                    RecipientResponse recipientResponse = new RecipientResponse(recipient, convocation);
                    recipientResponseRepository.save(recipientResponse);
                    return recipientResponse;
                }).collect(Collectors.toSet()));
    }

    public Convocation update(String uuid, String localAuthorityUuid, Convocation params) {

        Convocation convocation = getConvocation(uuid, localAuthorityUuid);
        boolean updated = false;
        List<String> updates = new ArrayList<>();

        // Add questions
        if (params.getQuestions() != null && params.getQuestions().size() > 0) {
            convocation.getQuestions().addAll(params.getQuestions());
            addHistory(convocation, HistoryType.QUESTIONS_ADDED,
                    params.getQuestions().stream().map(question -> question.getQuestion()).collect(Collectors.joining(", ")));
            updates.add("QUESTIONS_ADDED");
            updated = true;
        }

        // Comment
        if (StringUtils.isNotBlank(params.getComment()) && !params.getComment().equals(convocation.getComment())) {
            convocation.setComment(params.getComment());
            addHistory(convocation, HistoryType.COMMENT_MODIFIED, params.getComment());
            updates.add("COMMENT_MODIFIED");
            updated = true;
        }

        // Remove recipients that already an associated RecipientResponse
        if (params.getRecipients() != null) {
            params.setRecipients(params.getRecipients().stream()
                    .filter(recipient -> convocation.getRecipientResponses().stream().noneMatch(recipientResponse -> recipient.equals(recipientResponse.getRecipient())))
                    .collect(Collectors.toSet()));
        }

        // Create recipient responses
        if (params.getRecipients() != null && params.getRecipients().size() > 0) {
            convocation.getRecipientResponses().addAll(
                    params.getRecipients().stream().map(recipient -> {
                        RecipientResponse recipientResponse = new RecipientResponse(recipient, convocation);
                        recipientResponseRepository.save(recipientResponse);
                        return recipientResponse;
                    }).collect(Collectors.toSet()));
            addHistory(convocation, HistoryType.RECIPIENTS_ADDED,
                    params.getRecipients().stream().map(Recipient::getEmail).collect(Collectors.joining(", ")), false);
            applicationEventPublisher.publishEvent(new ConvocationRecipientAddedEvent(this, convocation, params.getRecipients()));
        }

        Convocation result = convocationRepository.saveAndFlush(convocation);
        if (updated) {
            applicationEventPublisher.publishEvent(new ConvocationUpdatedEvent(this, convocation, updates));
        }

        return result;
    }

    public Convocation getConvocation(String uuid, String localAuthorityUuid) {

        return convocationRepository
                .findByUuidAndLocalAuthorityUuid(uuid, localAuthorityUuid)
                .orElseThrow(() -> new NotFoundException("Convocation " + uuid + " not found in local authority " + localAuthorityUuid));
    }

    public void delete(Convocation convocation) {
        convocationRepository.delete(convocation);
    }

    public void cancelConvocation(Convocation convocation) {

        if (convocation.isCancelled()) {
            LOGGER.error("Try to cancel an already cancelled convocation : {}", convocation.getUuid());
            throw new ConvocationCancelledException();
        }
        convocation.setCancelled(true);
        convocation.setCancellationDate(LocalDateTime.now());
        convocationRepository.save(convocation);
        addHistory(convocation, HistoryType.CANCELLED);
        applicationEventPublisher.publishEvent(new ConvocationCancelledEvent(this, convocation));
    }

    public Convocation getByUuid(String uuid, String localAuthorityUuid) {
        return convocationRepository.findByUuidAndLocalAuthorityUuid(uuid, localAuthorityUuid).orElseThrow(ConvocationNotFoundException::new);
    }

    public void uploadFiles(Convocation convocation, MultipartFile file,
            MultipartFile procuration, MultipartFile[] annexes, String[] tagsIds)
            throws ConvocationFileException {

        Map<String, List<Tag>> tags = extractTags(tagsIds, convocation.getLocalAuthority().getUuid());

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
            Attachment attachment = saveAttachment(file, false, tags.get(file.getOriginalFilename()));
            convocation.setAttachment(attachment);
        }

        if (procuration != null) {
            Attachment attachment = saveAttachment(procuration, false, tags.get(procuration.getOriginalFilename()));
            convocation.setProcuration(attachment);
        }

        for (MultipartFile annexe : annexes) {
            Attachment attachment = saveAttachment(annexe, false, tags.get(annexe.getOriginalFilename()));
            convocation.getAnnexes().add(attachment);
        }

        convocationRepository.save(convocation);
    }

    public void uploadAdditionalFiles(Convocation convocation, MultipartFile[] annexes, String[] tagsIds)
            throws ConvocationFileException {

        if (annexes.length == 0)
            return;

        Map<String, List<Tag>> tags = extractTags(tagsIds, convocation.getLocalAuthority().getUuid());

        List<String> filenames = new ArrayList<>();
        for (MultipartFile annexe : annexes) {
            Attachment attachment = saveAttachment(annexe, true, tags.get(annexe.getOriginalFilename()));
            convocation.getAnnexes().add(attachment);
            filenames.add(attachment.getFilename());
        }

        convocationRepository.save(convocation);
        addHistory(convocation, HistoryType.ANNEXES_ADDED, String.join(", ", filenames.toArray(new String[0])));
        applicationEventPublisher.publishEvent(new ConvocationUpdatedEvent(this, convocation,
                Collections.singletonList("ANNEXES_ADDED")));
    }

    public void uploadMinutes(Convocation convocation, MultipartFile minutes) throws ConvocationNotAvailableException {

        if (convocation.getMeetingDate().isAfter(LocalDateTime.now())) {
            LOGGER.warn("Cannot upload minutes to convocation {}, meeting date is after now", convocation.getUuid());
            throw new ConvocationNotAvailableException("meetingAfter");
        }

        Attachment attachment = saveAttachment(minutes, false, null);
        convocation.setMinutes(attachment);
        convocationRepository.save(convocation);
        addHistory(convocation, HistoryType.MINUTES_ADDED);
        applicationEventPublisher.publishEvent(new MinutesAddedEvent(this, convocation));
    }

    public Attachment getFile(Convocation convocation, String fileUuid) throws NotFoundException {

        if (convocation.getAttachment().getUuid().equals(fileUuid)) {
            storageService.getAttachmentContent(convocation.getAttachment());
            return convocation.getAttachment();
        }

        if (convocation.getProcuration() != null && convocation.getProcuration().getUuid().equals(fileUuid)) {
            storageService.getAttachmentContent(convocation.getProcuration());
            return convocation.getProcuration();
        }

        if (convocation.getMinutes() != null && convocation.getMinutes().getUuid().equals(fileUuid)) {
            storageService.getAttachmentContent(convocation.getMinutes());
            return convocation.getMinutes();
        }

        for (Attachment annexe : convocation.getAnnexes()) {
            if (annexe.getUuid().equals(fileUuid)) {
                storageService.getAttachmentContent(annexe);
                return annexe;
            }
        }

        throw new NotFoundException("file not found");
    }

    public byte[] getStampedFile(byte[] content, LocalDateTime sentDate, LocalAuthority localAuthority, Integer x,
            Integer y) throws IOException, DocumentException {

        PdfReader pdfReader = new PdfReader(content);
        if (x == null || y == null) {
            if (pdfGeneratorUtil.pdfIsLandscape(pdfReader)) {
                //landscape case
                y = localAuthority.getStampPosition().getX();
                x = localAuthority.getStampPosition().getY();
            } else {
                //portrait case
                x = localAuthority.getStampPosition().getX();
                y = localAuthority.getStampPosition().getY();
            }
        }
        return pdfGeneratorUtil.stampPDF(sentDate, content, x, y);
    }

    public void answerConvocation(Convocation convocation, Recipient recipient, ResponseType responseType,
            String substituteUuid) {

        checkConvocationValidity(convocation);

        // Search recipient response object from recipient in convocation response list
        Optional<RecipientResponse> opt =
                convocation.getRecipientResponses().stream().filter(rr -> rr.getRecipient().equals(recipient)).findFirst();
        if (!opt.isPresent()) {
            LOGGER.error("Recipient {} not found in convocation {}", recipient.getUuid(), convocation.getUuid());
            throw new NotFoundException("Recipient " + recipient.getUuid() + " not found in convocation " + convocation.getUuid());
        }

        RecipientResponse recipientResponse = opt.get();

        // if (actual response is SUBSTITUED, we need to notify substitute that his procuration has been cancelled
        if (recipientResponse.getResponseType() == ResponseType.SUBSTITUTED) {
            RecipientResponse previous = new RecipientResponse(recipientResponse.getRecipient(),
                    recipientResponse.getSubstituteRecipient(), ResponseType.SUBSTITUTED);
            applicationEventPublisher.publishEvent(new ProcurationCancelledEvent(this, convocation, previous));
        }

        if (responseType == ResponseType.SUBSTITUTED) {

            // If recipient is a guest, he can't give a procuration to anyone else
            if (recipientResponse.isGuest()) {
                LOGGER.error("Recipient {} is a guest for convocation {}, procuration not allowed",
                        recipient.getUuid(), convocation.getUuid());
                throw new ProcurationNotPermittedException();
            }

            if (substituteUuid == null) {
                LOGGER.error("Recipient {} wants to give procuration but does not provide his substitute",
                        recipient.getUuid());
                throw new MissingParameterException("substituteUuid");
            }

            Optional<RecipientResponse> substituteResponse =
                    convocation.getRecipientResponses().stream().filter(rr -> rr.getRecipient().getUuid().equals(substituteUuid)).findFirst();
            if (!substituteResponse.isPresent()) {
                LOGGER.error("Recipient {} not found in convocation {} to receive procuration", substituteUuid,
                        convocation.getUuid());
                throw new NotFoundException("Recipient " + recipient.getUuid() + " not found in convocation " + convocation.getUuid() + " for procuration");
            } else {
                // if substitute is a guest, he can't receive a procuration
                if (substituteResponse.get().isGuest()) {
                    LOGGER.error("Procuration given to a guest ({}) for convocation {}. Not permitted",
                            substituteResponse.get().getRecipient().getUuid(), convocation.getUuid());
                    throw new ProcurationNotPermittedException();
                }
                recipientResponse.setSubstituteRecipient(substituteResponse.get().getRecipient());
                applicationEventPublisher.publishEvent(new ProcurationReceivedEvent(this, convocation, recipientResponse));
            }
        } else {
            // reset substitute if already set by a previous response
            recipientResponse.setSubstituteRecipient(null);
        }

        recipientResponse.setResponseType(responseType);
        convocationRepository.save(convocation);
        addHistory(convocation, HistoryType.CONVOCATION_RESPONSE,
                String.format("%s : %s",
                        recipient.getFullName(),
                        localesService.getMessage("fr", "convocation", "$.convocation.notifications." + responseType)), false);
        applicationEventPublisher.publishEvent(new ConvocationResponseEvent(this, convocation, recipientResponse));
    }

    public void answerQuestion(Convocation convocation, Recipient currentRecipient, String questionUuid,
            Boolean response) {

        checkConvocationValidity(convocation);

        Question question = convocation.getQuestions()
                .stream().filter(q -> q.getUuid().equals(questionUuid))
                .findFirst().orElseThrow(() -> {
                    LOGGER.error("Question {} not found in convocation {}", questionUuid,
                            convocation.getUuid());
                    return new NotFoundException();
                });

        Optional<QuestionResponse> opt =
                question.getResponses().stream().filter(questionResponse -> questionResponse.getRecipient().equals(currentRecipient)).findFirst();

        QuestionResponse questionResponse;
        if (!opt.isPresent()) {
            questionResponse = new QuestionResponse();
            questionResponse.setRecipient(currentRecipient);
            questionResponse.setQuestion(question);
            question.getResponses().add(questionResponse);
        } else {
            questionResponse = opt.get();
        }
        questionResponse.setResponse(response);
        questionResponseRepository.save(questionResponse);
        addHistory(convocation, HistoryType.QUESTION_RESPONSE,
                String.format("%s : %s : %s",
                        currentRecipient.getFullName(),
                        question.getQuestion(),
                        localesService.getMessage("fr", "convocation",
                                "$.convocation.export." + (response ? "yes" : "no"))), false);
    }

    public void convocationSent(Convocation convocation) {
        if (convocation != null && convocation.getUuid() != null) {
            convocationRepository.setSentDate(convocation.getUuid(), LocalDateTime.now());
            addHistory(convocation, HistoryType.SENT);
        }
    }

    public Profile retrieveProfile(String profileUuid) {
        JsonNode jsonProfile = externalRestService.getProfile(profileUuid);

        Profile profile = new Profile();
        if (jsonProfile == null)
            return profile;

        List<NotificationValue> notifications = new ArrayList<>();
        jsonProfile.get("notificationValues").forEach(notif -> {
            if (StringUtils.startsWith(notif.get("name").asText(), "CONVOCATION_")) {
                notifications.add(new NotificationValue(
                        notif.get("uuid").asText(),
                        StringUtils.removeStart(notif.get("name").asText(), "CONVOCATION_"),
                        notif.get("active").asBoolean()
                ));
            }
        });

        profile.setUuid(jsonProfile.get("uuid").asText(""));
        profile.setFirstname(jsonProfile.get("agent").get("given_name").asText(""));
        profile.setLastname(jsonProfile.get("agent").get("family_name").asText(""));
        profile.setEmail(jsonProfile.get("agent").get("email").asText(""));
        profile.setNotificationValues(notifications);

        return profile;
    }

    public List<Convocation> getConvocationsToRemind(int reminderDelay) {

        LocalDateTime start = LocalDateTime.now().plusDays(reminderDelay).toLocalDate().atStartOfDay();
        LocalDateTime end = LocalDateTime.now().plusDays(reminderDelay + 1).toLocalDate().atStartOfDay();

        return convocationRepository.findByCancelledFalseAndMeetingDateBetween(start, end);
    }

    public List<String> getNoResponseInformation() {

        return convocationRepository.findByHalfDuration();
    }

    public void addTagToFile(Convocation convocation, String fileUuid, String tagUuid) {

        Attachment file = getFile(convocation, fileUuid);
        Tag tag = tagService.getTag(tagUuid, convocation.getLocalAuthority().getUuid());

        file.getTags().add(tag);
        attachmentRepository.saveAndFlush(file);
    }

    public void removeTagFromFile(Convocation convocation, String fileUuid, String tagUuid) {

        Attachment file = getFile(convocation, fileUuid);
        Tag tag = tagService.getTag(tagUuid, convocation.getLocalAuthority().getUuid());

        file.getTags().remove(tag);
        attachmentRepository.saveAndFlush(file);
    }

    public ByteArrayOutputStream createArchive(Convocation convocation) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);

        if (convocation.getAttachment() != null) {
            storageService.getAttachmentContent(convocation.getAttachment());
            addEntry(convocation.getAttachment().getFilename(), convocation.getAttachment().getContent(), taos);
        }

        if (convocation.getAnnexes() != null) {
            for (Attachment annexe : convocation.getAnnexes()) {
                storageService.getAttachmentContent(annexe);
                addEntry(annexe.getFilename(), annexe.getContent(), taos);
            }
        }

        taos.close();
        baos.close();

        return compress(baos);
    }

    public Long countSentWithQuery(String multifield, LocalDate sentDateFrom, LocalDate sentDateTo, String
            assemblyType, LocalDate meetingDateFrom, LocalDate meetingDateTo, String subject,
            String filter, String currentLocalAuthUuid, Set<String> groups) {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Convocation> convocationRoot = query.from(Convocation.class);

        List<Predicate> predicates = getSentQueryPredicates(builder, convocationRoot, multifield, sentDateFrom,
                sentDateTo, assemblyType, meetingDateFrom, meetingDateTo, subject, filter, currentLocalAuthUuid,
                groups);
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
            String assemblyType, LocalDate meetingDateFrom, LocalDate meetingDateTo, String subject, String filter,
            Integer limit, Integer offset, String column, String direction,
            String currentLocalAuthUuid, Set<String> groups) {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Convocation> query = builder.createQuery(Convocation.class);
        Root<Convocation> convocationRoot = query.from(Convocation.class);

        query.select(convocationRoot);
        String columnAttribute = StringUtils.isEmpty(column) ? "meetingDate" : column;
        List<Predicate> predicates = getSentQueryPredicates(builder, convocationRoot, multifield, sentDateFrom,
                sentDateTo, assemblyType, meetingDateFrom, meetingDateTo, subject, filter, currentLocalAuthUuid,
                groups);

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
            String multifield, LocalDate sentDateFrom, LocalDate sentDateTo, String assemblyType, LocalDate
            meetingDateFrom, LocalDate meetingDateTo, String subject, String filter,
            String currentLocalAuthUuid, Set<String> groups) {

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(getGroupPredicate(builder, convocationRoot, groups));

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
                    builder.and(builder.equal(convocationRoot.get("assemblyType").get("uuid"), assemblyType)));

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

        if (meetingDateFrom == null && meetingDateTo == null && StringUtils.isNotBlank(filter)) {
            if (filter.equals("past"))
                predicates.add(
                        builder.and(builder.lessThan(convocationRoot.get("meetingDate"), LocalDateTime.now())));
            else if (filter.equals("future"))
                predicates.add(
                        builder.and(builder.greaterThan(convocationRoot.get("meetingDate"), LocalDateTime.now())));
        }

        if (StringUtils.isNotBlank(currentLocalAuthUuid)) {
            Join<LocalAuthority, Convocation> LocalAuthorityJoin = convocationRoot.join("localAuthority");
            LocalAuthorityJoin.on(builder.equal(LocalAuthorityJoin.get("uuid"), currentLocalAuthUuid));
        }

        return predicates;
    }

    private Predicate getGroupPredicate(CriteriaBuilder builder, Root<Convocation> convocationRoot, Set<String> groups) {
        if (!CollectionUtils.isEmpty(groups)) {
            return builder.or(convocationRoot.get("groupUuid").in(groups),
                    builder.equal(convocationRoot.get("groupUuid"), ""), builder.isNull(convocationRoot.get("groupUuid")));
        } else {
            return builder.or(builder.equal(convocationRoot.get("groupUuid"), ""), builder.isNull(convocationRoot.get("groupUuid")));
        }
    }

    private Attachment saveAttachment(MultipartFile file, boolean additionalDocument, List<Tag> tags) throws
            ConvocationFileException {

        try {
            Attachment attachment = new Attachment(file.getOriginalFilename(), file.getBytes(), additionalDocument);

            if (tags != null) {
                attachment.getTags().addAll(tags);
            }

            attachment = attachmentRepository.save(attachment);
            applicationEventPublisher.publishEvent(new FileUploadEvent(this, attachment));
            return attachment;
        } catch (IOException e) {
            throw new ConvocationFileException("Failed to get bytes from file " + file.getOriginalFilename(), e);
        }
    }

    private List<Predicate> getReceivedQueryPredicates(CriteriaBuilder
            builder, Root<Convocation> convocationRoot,
            String multifield, String assemblyType, LocalDate meetingDateFrom, LocalDate meetingDateTo, String
            subject,
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

    private void checkConvocationValidity(Convocation convocation) throws ConvocationCancelledException,
            ConvocationNotAvailableException {

        if (convocation.isCancelled()) {
            LOGGER.warn("Cannot answer to convocation {}, it has been cancelled", convocation.getUuid());
            throw new ConvocationCancelledException();
        }

        if (convocation.getMeetingDate().isBefore(LocalDateTime.now())) {
            LOGGER.warn("Cannot answer to convocation {}, it has been spent", convocation.getUuid());
            throw new ConvocationNotAvailableException("meetingBefore");
        }
    }

    private void addEntry(String entryName, byte[] content, TarArchiveOutputStream taos) throws IOException {
        File file = new File(entryName);
        FileCopyUtils.copy(content, file);
        ArchiveEntry archiveEntry = new TarArchiveEntry(file, entryName);
        taos.putArchiveEntry(archiveEntry);
        IOUtils.copy(new FileInputStream(file), taos);
        taos.closeArchiveEntry();
        file.delete();
    }

    private ByteArrayOutputStream compress(ByteArrayOutputStream baos) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        GzipCompressorOutputStream gcos = new GzipCompressorOutputStream(baos2);
        final byte[] buffer = new byte[2048];
        int n;
        while (-1 != (n = bais.read(buffer))) {
            gcos.write(buffer, 0, n);
        }
        gcos.close();
        bais.close();
        return baos2;
    }

    private void addHistory(Convocation convocation, HistoryType type) {
        addHistory(convocation, type, null, true);
    }

    private void addHistory(Convocation convocation, HistoryType type, String message) {
        addHistory(convocation, type, message, true);
    }

    private void addHistory(Convocation convocation, HistoryType type, String message, boolean publicHistory) {
        applicationEventPublisher.publishEvent(new HistoryEvent(this, convocation, type, message, publicHistory));
    }

    private Map<String, List<Tag>> extractTags(String[] tagsIds, String localAuthorityUuid) {

        Map<String, List<Tag>> tags = new HashMap<>();
        if (tagsIds != null) {
            for (String key : tagsIds) {
                String[] words = key.split("/");
                if (words.length >= 2) {
                    // first : filename
                    // rest of array : tags uuids
                    List<Tag> tagList = new ArrayList<>();
                    for (String tagId : Arrays.copyOfRange(words, 1, words.length)) {
                        try {
                            tagList.add(tagService.getTag(tagId, localAuthorityUuid));
                        } catch (NotFoundException e) {
                            LOGGER.error("Tag {} does not belong to local authority {}", tagId, localAuthorityUuid);
                        }
                    }
                    tags.put(words[0], tagList);
                }
            }
        }

        return tags;
    }
}


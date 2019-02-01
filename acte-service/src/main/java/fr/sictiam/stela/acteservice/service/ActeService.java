package fr.sictiam.stela.acteservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfReader;
import fr.sictiam.stela.acteservice.dao.ActeExportRepository;
import fr.sictiam.stela.acteservice.dao.ActeHistoryRepository;
import fr.sictiam.stela.acteservice.dao.ActeRepository;
import fr.sictiam.stela.acteservice.dao.AttachmentRepository;
import fr.sictiam.stela.acteservice.model.*;
import fr.sictiam.stela.acteservice.model.event.ActeHistoryEvent;
import fr.sictiam.stela.acteservice.model.ui.ActeCSVUI;
import fr.sictiam.stela.acteservice.model.ui.ActeUuidsAndSearchUI;
import fr.sictiam.stela.acteservice.service.exceptions.*;
import fr.sictiam.stela.acteservice.service.exceptions.FileNotFoundException;
import fr.sictiam.stela.acteservice.service.util.PdfGeneratorUtil;
import fr.sictiam.stela.acteservice.service.util.ZipGeneratorUtil;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.validation.ObjectError;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.validation.constraints.NotNull;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ActeService implements ApplicationListener<ActeHistoryEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActeService.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final ActeRepository acteRepository;
    private final ActeHistoryRepository acteHistoryRepository;
    private final AttachmentRepository attachmentRepository;
    private final ActeExportRepository acteExportRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final LocalAuthorityService localAuthorityService;
    private final LocalesService localesService;
    private final ArchiveService archiveService;
    private final PdfGeneratorUtil pdfGeneratorUtil;
    private final ZipGeneratorUtil zipGeneratorUtil;
    private final ExternalRestService externalRestService;

    @Value("${application.miat.url}")
    private String acteUrl;

    @Value("${application.miat.rescueUrl}")
    private String rescueUrl;

    @Autowired
    @Qualifier("miatRestTemplate")
    private RestTemplate miatRestTemplate;

    @Autowired
    public ActeService(ActeRepository acteRepository, ActeHistoryRepository acteHistoryRepository,
            AttachmentRepository attachmentRepository, ApplicationEventPublisher applicationEventPublisher,
            LocalAuthorityService localAuthorityService, ArchiveService archiveService,
            PdfGeneratorUtil pdfGeneratorUtil, ZipGeneratorUtil zipGeneratorUtil, LocalesService localesService,
            ExternalRestService externalRestService, ActeExportRepository acteExportRepository) {
        this.acteRepository = acteRepository;
        this.acteHistoryRepository = acteHistoryRepository;
        this.attachmentRepository = attachmentRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.localAuthorityService = localAuthorityService;
        this.archiveService = archiveService;
        this.pdfGeneratorUtil = pdfGeneratorUtil;
        this.zipGeneratorUtil = zipGeneratorUtil;
        this.localesService = localesService;
        this.externalRestService = externalRestService;
        this.acteExportRepository = acteExportRepository;
    }

    public Acte create(String number, String objet, ActeNature nature, String code, LocalDate decision,
            Boolean isPublic, Boolean isPublicWebsite, String groupUuid, MultipartFile file, String fileType,
            MultipartFile[] annexes, String[] annexeTypes, String email, LocalAuthority localAuthority)
            throws IOException {
        Attachment acteFile = new Attachment(file.getBytes(), file.getOriginalFilename(), file.getSize(), fileType);
        List<Attachment> annexeFiles = new ArrayList<>();
        for (MultipartFile annexe : annexes) {
            annexeFiles.add(new Attachment(annexe.getBytes(), annexe.getOriginalFilename(), annexe.getSize(),
                    getCodeForAnnexeFilename(annexeTypes, annexe.getOriginalFilename())));
        }
        JsonNode node = externalRestService.getProfileForEmail(localAuthority.getSiren(), email);
        String profileUuid = node != null ? node.get("uuid").asText() : localAuthority.getGenericProfileUuid();

        Acte acte = new Acte(number, objet, nature, code, LocalDateTime.now(), decision, isPublic, isPublicWebsite,
                groupUuid, acteFile, annexeFiles, profileUuid, localAuthority);
        return publishActe(acte);
    }

    private String getCodeForAnnexeFilename(String[] annexeTypes, String filename) {
        for (String annexeType : annexeTypes) {
            Matcher m = Pattern.compile("(.*):(.*)").matcher(annexeType); // [filename]:[code_type]
            if (m.find() && m.groupCount() > 1 && filename.equals(m.group(1))) return m.group(2);
        }
        return "CO_DE";
    }

    public JsonNode getGroups(String localAuthorityUuid) throws IOException {
        return externalRestService.getGroupsForLocalAuthority(localAuthorityUuid);
    }

    public Acte publishActe(Acte acte) {
        Acte created = acteRepository.save(acte);

        ActeHistory acteHistory = new ActeHistory(acte.getUuid(), StatusType.CREATED, Flux.TRANSMISSION_ACTE);
        applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory));

        LOGGER.info("Acte {} created with id {}", created.getNumber(), created.getUuid());

        return created;
    }

    public void rePublishActe(String acteUuid) {

        ActeHistory acteHistory = new ActeHistory(acteUuid, StatusType.RECREATED, Flux.TRANSMISSION_ACTE);
        applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory));

        LOGGER.info("Acte recreated with id {}", acteUuid);
    }

    public Acte findByIdActe(String iDActe) {
        String[] idActeSplit = iDActe.split("-");
        String siren = idActeSplit[1];
        String number = idActeSplit[3];
        return acteRepository.findByNumberAndLocalAuthoritySirenAndDraftNull(number, siren).orElseThrow(ActeNotFoundException::new);
    }

    public boolean isNumberAvailable(String number, LocalDate decisionDate, ActeNature nature, String localAuthorityUuid) {
        return !acteRepository.findFirstByNumberAndDecisionAndNatureAndLocalAuthority_UuidAndDraftNull(number, decisionDate, nature, localAuthorityUuid).isPresent();
    }

    public List<ObjectError> metierValidation(Acte acte, List<ObjectError> errors) {
        if (errors == null) errors = new ArrayList<>();
        if (!isNumberAvailable(acte.getNumber(), acte.getDecision(), acte.getNature(), acte.getLocalAuthority().getUuid())) {
            errors.add(new ObjectError("number", "notifications.acte.sent.error.number_unavailable"));
        }
        return errors;
    }

    public Acte receiveAREvent(String iDActe, StatusType statusType, Attachment attachment) {
        Acte acte = findByIdActe(iDActe);
        ActeHistory acteHistory = new ActeHistory(acte.getUuid(), statusType, LocalDateTime.now(), attachment.getFile(),
                attachment.getFilename());
        applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory));
        LOGGER.info("Acte {} {} with id {}", acte.getNumber(), statusType, acte.getUuid());
        return acte;
    }

    public void receiveAnomalieActe(String siren, String number, String detail, Attachment attachment) {
        Acte acte = acteRepository.findByNumberAndLocalAuthoritySirenAndDraftNull(number, siren)
                .orElseThrow(ActeNotFoundException::new);
        ActeHistory acteHistory = new ActeHistory(acte.getUuid(), StatusType.NACK_RECEIVED, LocalDateTime.now(),
                attachment.getFile(), attachment.getFilename(), detail);
        applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory));
        LOGGER.info("Acte {} anomalie with id {}", acte.getNumber(), acte.getUuid());
    }

    public void receiveAnomalieEnveloppe(String anomalieFilename, String detail, Attachment attachment) {
        String[] splited = anomalieFilename.split("--");
        splited = Arrays.copyOfRange(splited, 1, splited.length);
        String sourceTarGz = String.join("--", splited).replace(".xml", ".tar.gz");
        ActeHistory acteHistorySource = acteHistoryRepository.findFirstByFileNameContaining(sourceTarGz)
                .orElseThrow(ActeNotFoundException::new);
        Acte acte = acteRepository.findByUuidAndDraftNull(acteHistorySource.getActeUuid())
                .orElseThrow(ActeNotFoundException::new);
        ActeHistory acteHistory = new ActeHistory(acte.getUuid(), StatusType.NACK_RECEIVED, LocalDateTime.now(),
                attachment.getFile(), attachment.getFilename(), detail);
        applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory));
        LOGGER.info("Acte {} anomalie with id {}", acte.getNumber(), acte.getUuid());
    }

    public void receiveAdditionalPiece(StatusType status, String idActe, Attachment attachment, String message,
            Flux flux) {
        Acte acte = findByIdActe(idActe);
        ActeHistory acteHistory = new ActeHistory(acte.getUuid(), status, LocalDateTime.now(), attachment.getFile(),
                attachment.getFilename(), flux);
        if (StringUtils.isNotBlank(message)) {
            acteHistory.setMessage(message);
        }
        applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory));
        LOGGER.info("Acte {} addtional piece with id {}", acte.getNumber(), acte.getUuid());
    }

    public void registerAdditionalPieces(StatusType status, String acteUuid, List<Attachment> attachments,
            String message, Flux flux) {
        Acte acte = getByUuid(acteUuid);
        Attachment attachment = attachments.get(0);
        ActeHistory acteHistory = new ActeHistory(acte.getUuid(), status, LocalDateTime.now(),
                attachments.size() > 1 ? null : attachment.getFile(),
                attachments.size() > 1 ? null : attachment.getFilename(), flux);
        if (StringUtils.isNotBlank(message)) {
            acteHistory.setMessage(message);
        }
        applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory, attachments));
        LOGGER.info("Acte {} addtional piece with id {}", acte.getNumber(), acte.getUuid());
    }

    public void receiveDefere(StatusType status, String idActe, List<Attachment> attachments, String message)
            throws IOException {
        Acte acte = findByIdActe(idActe);
        byte[] file;
        String fileName;
        if (attachments.size() == 1) {
            file = attachments.get(0).getFile();
            fileName = attachments.get(0).getFilename();
        } else {
            fileName = "DefereTA_" + idActe + ".zip";
            file = zipGeneratorUtil.createZip(
                    attachments.stream().collect(Collectors.toMap(Attachment::getFilename, Attachment::getFile)));

        }
        ActeHistory acteHistory = new ActeHistory(acte.getUuid(), status, LocalDateTime.now(), file, fileName);
        if (StringUtils.isNotBlank(message)) {
            acteHistory.setMessage(message);
        }
        applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory));
        LOGGER.info("Acte {} defere with id {}", acte.getNumber(), acte.getUuid());

    }

    public Long countAllWithQuery(String multifield, String number, String objet, ActeNature nature,
            LocalDate decisionFrom, LocalDate decisionTo, StatusType status, String currentLocalAuthUuid,
            Set<String> groups) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Acte> acteRoot = query.from(Acte.class);

        List<Predicate> predicates = getAllQueryPredicates(builder, acteRoot, multifield, number, objet, nature,
                decisionFrom, decisionTo, status, currentLocalAuthUuid, groups);
        query.select(builder.count(acteRoot));
        query.where(predicates.toArray(new Predicate[predicates.size()]));

        return entityManager.createQuery(query).getSingleResult();
    }

    public Long countAllPublicWithQuery(String multifield, String number, String objet, String siren,
            LocalDate decisionFrom, LocalDate decisionTo) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Acte> acteRoot = query.from(Acte.class);

        List<Predicate> predicates = getAllPublicQueryPredicates(builder, acteRoot, multifield, number, objet, siren,
                decisionFrom, decisionTo);
        query.select(builder.count(acteRoot));
        query.where(predicates.toArray(new Predicate[predicates.size()]));

        return entityManager.createQuery(query).getSingleResult();
    }

    public List<Acte> getAllPublicWithQuery(String multifield, String number, String objet, String siren,
            LocalDate decisionFrom, LocalDate decisionTo, Integer limit, Integer offset, String column,
            String direction) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Acte> query = builder.createQuery(Acte.class);
        Root<Acte> acteRoot = query.from(Acte.class);

        String columnAttribute = StringUtils.isEmpty(column) ? "creation" : column;
        List<Predicate> predicates = getAllPublicQueryPredicates(builder, acteRoot, multifield, number, objet, siren,
                decisionFrom, decisionTo);

        Subquery<ActeHistory> subquery = query.subquery(ActeHistory.class);
        Root<ActeHistory> historyTable = subquery.from(ActeHistory.class);
        subquery
                .select(historyTable.get("acteUuid")).distinct(true)
                .where(historyTable.get("status")
                        .in(Collections.singleton(StatusType.ACK_RECEIVED)));

        Subquery<ActeHistory> subquery2 = query.subquery(ActeHistory.class);
        Root<ActeHistory> historyTable2 = subquery2.from(ActeHistory.class);
        subquery2
                .select(historyTable2.get("acteUuid")).distinct(true)
                .where(historyTable2.get("status")
                        .in(Arrays.asList(StatusType.CANCELLED, StatusType.CANCELLATION_ARCHIVE_CREATED, StatusType.CANCELLATION_ASKED)));

        predicates.add(acteRoot.get("uuid").in(subquery));
        predicates.add(builder.not(acteRoot.get("uuid").in(subquery2)));


        query.where(predicates.toArray(new Predicate[predicates.size()]))
                .orderBy(!StringUtils.isEmpty(direction) && direction.equals("ASC")
                        ? builder.asc(acteRoot.get(columnAttribute))
                        : builder.desc(acteRoot.get(columnAttribute)));

        return entityManager.createQuery(query).setFirstResult(offset).setMaxResults(limit).getResultList();
    }

    public List<Acte> getAllFull(Integer limit, Integer offset, String column, String direction,
            String currentLocalAuthUuid) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Acte> query = builder.createQuery(Acte.class);
        Root<Acte> acteRoot = query.from(Acte.class);

        String columnAttribute = StringUtils.isEmpty(column) ? "creation" : column;
        query.orderBy(!StringUtils.isEmpty(direction) && direction.equals("ASC")
                ? builder.asc(acteRoot.get(columnAttribute))
                : builder.desc(acteRoot.get(columnAttribute)));

        return entityManager.createQuery(query).setFirstResult(offset).setMaxResults(limit).getResultList();
    }

    public List<Acte> getAllWithQueryNoSearch(Integer limit, Integer offset, String column, String direction,
            String currentLocalAuthUuid) {
        return getAllWithQuery(null, null, null, null, null, null,
                null, limit, offset, column, direction, currentLocalAuthUuid, Collections.emptySet());
    }

    public List<Acte> getAllWithQuery(String multifield, String number, String objet, ActeNature nature,
            LocalDate decisionFrom, LocalDate decisionTo, StatusType status, Integer limit, Integer offset,
            String column, String direction, String currentLocalAuthUuid, Set<String> groups) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Acte> query = builder.createQuery(Acte.class);
        Root<Acte> acteRoot = query.from(Acte.class);

        query.select(builder.construct(Acte.class,
                acteRoot.get("uuid"),
                acteRoot.get("objet"),
                acteRoot.get("creation"),
                acteRoot.get("decision"),
                acteRoot.get("number"),
                acteRoot.get("nature"),
                acteRoot.get("lastHistoryDate"),
                acteRoot.get("lastHistoryStatus"),
                acteRoot.get("lastHistoryFlux")
        ));

        String columnAttribute = StringUtils.isEmpty(column) ? "creation" : column;
        List<Predicate> predicates = getAllQueryPredicates(builder, acteRoot, multifield, number, objet, nature,
                decisionFrom, decisionTo, status, currentLocalAuthUuid, groups);
        query.where(predicates.toArray(new Predicate[predicates.size()]))
                .orderBy(!StringUtils.isEmpty(direction) && direction.equals("ASC")
                        ? builder.asc(acteRoot.get(columnAttribute))
                        : builder.desc(acteRoot.get(columnAttribute)));

        return entityManager.createQuery(query).setFirstResult(offset).setMaxResults(limit).getResultList();
    }

    private List<Predicate> getAllPublicQueryPredicates(CriteriaBuilder builder, Root<Acte> acteRoot, String multifield,
            String number, String objet, String siren, LocalDate decisionFrom, LocalDate decisionTo) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(getNotDraftPredicate(builder, acteRoot));
        predicates.add(getPublicPredicate(builder, acteRoot));
        predicates.add(getNaturePredicate(builder, acteRoot,
                Arrays.asList(ActeNature.DELIBERATIONS, ActeNature.ARRETES_REGLEMENTAIRES)));
        if (StringUtils.isNotBlank(multifield)) predicates.add(getObjetMultifield(builder, acteRoot, multifield));
        if (StringUtils.isNotBlank(number)) predicates.add(getNumberPredicate(builder, acteRoot, number));
        if (StringUtils.isNotBlank(objet)) predicates.add(getObjetPredicate(builder, acteRoot, objet));
        if (decisionFrom != null && decisionTo != null)
            predicates.add(getDecisionPredicate(builder, acteRoot, decisionFrom, decisionTo));
        if (StringUtils.isNotBlank(siren)) {
            Join<LocalAuthority, Acte> LocalAuthorityJoin = acteRoot.join("localAuthority");
            LocalAuthorityJoin.on(builder.equal(LocalAuthorityJoin.get("siren"), siren));
        }
        return predicates;
    }

    private List<Predicate> getAllQueryPredicates(CriteriaBuilder builder, Root<Acte> acteRoot, String multifield,
            String number, String objet, ActeNature nature, LocalDate decisionFrom, LocalDate decisionTo,
            StatusType status, String currentLocalAuthUuid, Set<String> groups) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(getNotDraftPredicate(builder, acteRoot));
        predicates.add(getGroupPredicate(builder, acteRoot, groups));
        if (StringUtils.isNotBlank(multifield)) predicates.add(getObjetMultifield(builder, acteRoot, multifield));
        if (StringUtils.isNotBlank(number)) predicates.add(getNumberPredicate(builder, acteRoot, number));
        if (StringUtils.isNotBlank(objet)) predicates.add(getObjetPredicate(builder, acteRoot, objet));
        if (nature != null) predicates.add(getNaturePredicate(builder, acteRoot, Collections.singletonList(nature)));
        if (status != null) predicates.add(getStatusPredicate(builder, acteRoot, status));
        if (decisionFrom != null && decisionTo != null)
            predicates.add(getDecisionPredicate(builder, acteRoot, decisionFrom, decisionTo));
        if (StringUtils.isNotBlank(currentLocalAuthUuid)) {
            Join<LocalAuthority, Acte> LocalAuthorityJoin = acteRoot.join("localAuthority");
            LocalAuthorityJoin.on(builder.equal(LocalAuthorityJoin.get("uuid"), currentLocalAuthUuid));
        }
        return predicates;
    }

    private Predicate getObjetMultifield(CriteriaBuilder builder, Root<Acte> acteRoot, String multifield) {
        return builder.or(
                builder.like(builder.lower(acteRoot.get("number")), "%" + multifield.toLowerCase() + "%"),
                builder.like(builder.lower(acteRoot.get("objet")), "%" + multifield.toLowerCase() + "%"));
    }

    private Predicate getNumberPredicate(CriteriaBuilder builder, Root<Acte> acteRoot, String number) {
        return builder.and(builder.like(builder.lower(acteRoot.get("number")), "%" + number.toLowerCase() + "%"));
    }

    private Predicate getObjetPredicate(CriteriaBuilder builder, Root<Acte> acteRoot, String objet) {
        return builder.and(builder.like(builder.lower(acteRoot.get("objet")), "%" + objet.toLowerCase() + "%"));
    }

    private Predicate getNotDraftPredicate(CriteriaBuilder builder, Root<Acte> acteRoot) {
        return builder.and(builder.isNull(acteRoot.get("draft")));
    }

    private Predicate getPublicPredicate(CriteriaBuilder builder, Root<Acte> acteRoot) {
        return builder.and(builder.isTrue(acteRoot.get("isPublic")));
    }

    private Predicate getNaturePredicate(CriteriaBuilder builder, Root<Acte> acteRoot, List<ActeNature> natures) {
        List<Predicate> naturesPredicate = natures.stream()
                .map(nature -> builder.equal(acteRoot.get("nature"), nature))
                .collect(Collectors.toList());
        return builder.or(naturesPredicate.toArray(new Predicate[naturesPredicate.size()]));
    }

    private Predicate getGroupPredicate(CriteriaBuilder builder, Root<Acte> acteRoot, Set<String> groups) {
        if (!CollectionUtils.isEmpty(groups)) {
            return builder.or(acteRoot.get("groupUuid").in(groups),
                    builder.equal(acteRoot.get("groupUuid"), ""), builder.isNull(acteRoot.get("groupUuid")));
        } else {
            return builder.or(builder.equal(acteRoot.get("groupUuid"), ""), builder.isNull(acteRoot.get("groupUuid")));
        }
    }

    private Predicate getDecisionPredicate(CriteriaBuilder builder, Root<Acte> acteRoot, LocalDate decisionFrom,
            LocalDate decisionTo) {
        return builder.and(builder.between(acteRoot.get("decision"), decisionFrom, decisionTo));
    }

    private Predicate getStatusPredicate(CriteriaBuilder builder, Root<Acte> acteRoot, StatusType status) {
        return builder.and(builder.equal(acteRoot.get("lastHistoryStatus"), status));
    }

    public List<ActeHistory> getPrefectureReturns(String currentLocalAuthUuid, LocalDateTime date) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ActeHistory> query = cb.createQuery(ActeHistory.class);
        Root<ActeHistory> acteHistoryTable = query.from(ActeHistory.class);
        query.select(acteHistoryTable);

        Subquery<String> acteQuery = query.subquery(String.class);
        Root<Acte> acteTable = acteQuery.from(Acte.class);

        acteQuery.select(acteTable.get("uuid"));
        Join<LocalAuthority, Acte> localAuthorityJoin = acteTable.join("localAuthority");
        localAuthorityJoin.on(cb.equal(localAuthorityJoin.get("uuid"), currentLocalAuthUuid));

        List<Predicate> mainQueryPredicates = new ArrayList<Predicate>();

        mainQueryPredicates.add(acteHistoryTable.get("status")
                .in(Arrays.asList(StatusType.COURRIER_SIMPLE_RECEIVED, StatusType.ACK_RECEIVED,
                        StatusType.COURRIER_SIMPLE_RECEIVED, StatusType.LETTRE_OBSERVATION_RECEIVED,
                        StatusType.ACK_REPONSE_LETTRE_OBSERVATION, StatusType.ACK_REPONSE_PIECE_COMPLEMENTAIRE,
                        StatusType.DEFERE_RECEIVED, StatusType.NACK_RECEIVED, StatusType.CANCELLED)));
        mainQueryPredicates.add(acteHistoryTable.get("acteUuid").in(acteQuery));
        mainQueryPredicates.add(cb.and(cb.greaterThan(acteHistoryTable.get("date"), date)));
        query.where(mainQueryPredicates.toArray(new Predicate[]{}));
        TypedQuery<ActeHistory> typedQuery = entityManager.createQuery(query);
        List<ActeHistory> resultList = typedQuery.getResultList();

        return resultList;
    }

    public Acte getByUuid(String uuid) {
        return acteRepository.findByUuidAndDraftNull(uuid).orElseThrow(ActeNotFoundException::new);
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
        if (isActeACK(uuid)) {
            ActeHistory acteHistory = new ActeHistory(uuid, StatusType.CANCELLATION_ASKED,
                    Flux.ANNULATION_TRANSMISSION);
            applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory));
        } else
            throw new CancelForbiddenException();
    }

    public void sent(String acteUuid, Flux flux) {
        ActeHistory acteHistory = new ActeHistory(acteUuid, StatusType.SENT, flux);
        applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory));
    }

    public void notSent(String acteUuid, Flux flux) {
        ActeHistory acteHistory = new ActeHistory(acteUuid, StatusType.NOT_SENT, flux);
        applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory));
    }

    public void persistActeExport(PendingMessage pendingMessage) {
        LOGGER.info("Persisting acte sent infos");
        Acte acte = acteRepository.findByUuid(pendingMessage.getActeUuid()).get();
        ActeExport acteExport = new ActeExport(pendingMessage.getActeUuid(), ZonedDateTime.now(),
                pendingMessage.getFileName(), acte.getLocalAuthority().getSiren(),
                acte.getLocalAuthority().getDepartment(), acte.getLocalAuthority().getDistrict());
        try {
            JsonNode node = externalRestService.getProfile(acte.getProfileUuid());
            acteExport.setAgentFirstName(node.get("agent").get("given_name").asText());
            acteExport.setAgentName(node.get("agent").get("family_name").asText());
            acteExport.setAgentEmail(node.get("agent").get("email").asText());
        } catch (Exception e) {
            LOGGER.error("Error while retrieving profile infos : {}", e.getMessage());
        }
        try {
            InputStream inputStream = new ByteArrayInputStream(pendingMessage.getFile());
            TarArchiveInputStream tarArchiveInputStream =
                    new TarArchiveInputStream(new GzipCompressorInputStream(inputStream));
            TarArchiveEntry entry;
            List<String> fileNameList = new ArrayList<>();
            while ((entry = tarArchiveInputStream.getNextTarEntry()) != null) {
                LOGGER.info("File found in archive:" + entry.getName());
                fileNameList.add(entry.getName());
            }
            acteExport.setFilesNameList(String.join(";", fileNameList));
        } catch (IOException e) {
            LOGGER.error("Error while extracting .tar.gz : {}", e.getMessage());
        }
        acteExportRepository.save(acteExport);
    }

    public void sendReponseCourrierSimple(String uuid, MultipartFile file) throws IOException {
        Attachment attachment = new Attachment(file.getBytes(), file.getOriginalFilename(), file.getSize());
        registerAdditionalPieces(StatusType.REPONSE_COURRIER_SIMPLE_ASKED, uuid, Collections.singletonList(attachment),
                null, Flux.REPONSE_COURRIER_SIMPLE);
    }

    public void sendReponseLettreObservation(String uuid, String reponseOrRejet, MultipartFile file)
            throws IOException {
        Attachment attachment = new Attachment(file.getBytes(), file.getOriginalFilename(), file.getSize());
        if (reponseOrRejet.equals("reponse")) {
            registerAdditionalPieces(StatusType.REPONSE_LETTRE_OBSEVATION_ASKED, uuid,
                    Collections.singletonList(attachment), null, Flux.REPONSE_LETTRE_OBSEVATION);
        } else {
            registerAdditionalPieces(StatusType.REJET_LETTRE_OBSERVATION_ASKED, uuid,
                    Collections.singletonList(attachment), null, Flux.REFUS_EXPLICITE_LETTRE_OBSERVATION);
        }
    }

    public void sendReponsePiecesComplementaires(String uuid, String reponseOrRejet, MultipartFile[] files)
            throws IOException {
        List<Attachment> attachments = new ArrayList<>();
        for (MultipartFile file : files) {
            attachments.add(new Attachment(file.getBytes(), file.getOriginalFilename(), file.getSize()));
        }
        if (reponseOrRejet.equals("reponse")) {
            registerAdditionalPieces(StatusType.PIECE_COMPLEMENTAIRE_ASKED, uuid, attachments, null,
                    Flux.TRANSMISSION_PIECES_COMPLEMENTAIRES);
        } else {
            registerAdditionalPieces(StatusType.REFUS_PIECES_COMPLEMENTAIRE_ASKED, uuid, attachments, null,
                    Flux.REFUS_EXPLICITE_TRANSMISSION_PIECES_COMPLEMENTAIRES);
        }
    }

    public boolean isActeACK(String uuid) {
        return isActeACK(getByUuid(uuid));
    }

    public boolean isActeACK(Acte acte) {
        // TODO: Improve later when phases will be supported
        List<StatusType> forbidenStatus = Arrays.asList(StatusType.CANCELLATION_ASKED,
                StatusType.CANCELLATION_ARCHIVE_CREATED, StatusType.CANCELLED);
        SortedSet<ActeHistory> acteHistoryList = acte.getActeHistories();
        return acteHistoryList.stream().anyMatch(acteHistory -> acteHistory.getStatus().equals(StatusType.ACK_RECEIVED))
                && acteHistoryList.stream().noneMatch(acteHistory -> forbidenStatus.contains(acteHistory.getStatus()));
    }

    public ActeHistory getLastMetierHistory(String uuid) {
        // TODO: Improve later on with status improvements
        List<StatusType> statusOrdered = Arrays.asList(StatusType.CREATED, StatusType.ANTIVIRUS_KO, StatusType.SENT,
                StatusType.ACK_RECEIVED, StatusType.NACK_RECEIVED, StatusType.CANCELLATION_ASKED, StatusType.CANCELLED,
                StatusType.ARCHIVE_TOO_LARGE, StatusType.FILE_ERROR, StatusType.COURRIER_SIMPLE_RECEIVED,
                StatusType.DEMANDE_PIECE_COMPLEMENTAIRE_RECEIVED, StatusType.LETTRE_OBSERVATION_RECEIVED,
                StatusType.DEFERE_RECEIVED, StatusType.REPONSE_LETTRE_OBSEVATION_ASKED,
                StatusType.REJET_LETTRE_OBSERVATION_ASKED, StatusType.REFUS_PIECES_COMPLEMENTAIRE_ASKED,
                StatusType.PIECE_COMPLEMENTAIRE_ASKED, StatusType.REPONSE_COURRIER_SIMPLE_ASKED,
                StatusType.ACK_REPONSE_PIECE_COMPLEMENTAIRE, StatusType.ACK_REPONSE_LETTRE_OBSERVATION);
        SortedSet<ActeHistory> acteHistories = getByUuid(uuid).getActeHistories();
        if (acteHistories.size() == 0)
            return null;
        List<ActeHistory> metierHistories = acteHistories.stream()
                .filter(acteHistory -> statusOrdered.stream()
                        .anyMatch(statusType -> acteHistory.getStatus().equals(statusType)))
                .collect(Collectors.toList());
        return metierHistories.get(metierHistories.size() - 1);
    }

    public List<ActeCSVUI> getActesCSV(ActeUuidsAndSearchUI acteUuidsAndSearchUI, String language) {
        if (StringUtils.isBlank(language))
            language = "fr";
        ClassPathResource classPathResource = new ClassPathResource("/locales/" + language + "/acte.json");
        List<Acte> actes = getActesFromUuidsOrSearch(acteUuidsAndSearchUI);
        List<ActeCSVUI> acteCSVUIs = new ArrayList<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(classPathResource.getInputStream());
            for (Acte acte : actes) {
                acteCSVUIs.add(new ActeCSVUI(acte.getNumber(), acte.getObjet(), acte.getDecision().toString(),
                        jsonNode.get("acte").get("nature").get(acte.getNature().toString()).asText(),
                        jsonNode.get("acte").get("status")
                                .get(acte.getActeHistories().last().getStatus().toString()).asText()));
            }
        } catch (Exception e) {
            LOGGER.error("Error while trying to translate Acte values, will take untranslated values: {}", e);
            for (Acte acte : actes) {
                acteCSVUIs.add(new ActeCSVUI(acte.getNumber(), acte.getObjet(), acte.getDecision().toString(),
                        acte.getNature().toString(), acte.getActeHistories().last().getStatus().toString()));
            }
        }
        if (acteCSVUIs.size() == 0)
            throw new NoContentException();
        return acteCSVUIs;
    }

    public List<String> getTranslatedCSVFields(List<String> fields, String language) {
        if (StringUtils.isBlank(language))
            language = "fr";
        ClassPathResource classPathResource = new ClassPathResource("/locales/" + language + "/acte.json");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(classPathResource.getInputStream());
            List<String> translatedList = new ArrayList<>();
            for (String field : fields)
                translatedList.add(jsonNode.get("acte").get("fields").get(field).asText());
            return translatedList;
        } catch (Exception e) {
            LOGGER.error("Error while trying to translate CSV fields, will take untranslated fields: {}", e);
            return fields;
        }
    }

    private List<Acte> getActesFromUuidsOrSearch(ActeUuidsAndSearchUI ui) {
        return ui.getUuids().size() > 0 ? ui.getUuids().stream().map(this::getByUuid).collect(Collectors.toList())
                : getAllWithQuery(ui.getMultifield(), ui.getNumber(), ui.getObjet(), ui.getNature(),
                ui.getDecisionFrom(), ui.getDecisionTo(), ui.getStatus(), 1, 0, "", "", null, null);
    }

    public List<Acte> getAckedActeFromUuidsOrSearch(ActeUuidsAndSearchUI acteUuidsAndSearchUI) {
        List<Acte> actes = getActesFromUuidsOrSearch(acteUuidsAndSearchUI).stream().filter(this::isActeACK)
                .collect(Collectors.toList());
        if (actes.isEmpty())
            throw new NoContentException();
        return actes;
    }

    public byte[] getMergedStampedAttachments(ActeUuidsAndSearchUI acteUuidsAndSearchUI,
            LocalAuthority currentLocalAuthority) throws IOException, DocumentException {
        List<Acte> actes = getAckedActeFromUuidsOrSearch(acteUuidsAndSearchUI);
        List<byte[]> stampPdfs = new ArrayList<>();
        for (Acte acte : actes) {
            stampPdfs.add(getStampedActe(acte, null, null, currentLocalAuthority));
        }
        return pdfGeneratorUtil.mergePDFs(stampPdfs);
    }

    public byte[] getZipedStampedAttachments(ActeUuidsAndSearchUI acteUuidsAndSearchUI,
            LocalAuthority currentLocalAuthority) throws IOException, DocumentException {
        List<Acte> actes = getAckedActeFromUuidsOrSearch(acteUuidsAndSearchUI);
        Map<String, byte[]> pdfs = new HashMap<>();
        for (Acte acte : actes) {
            pdfs.put(acte.getActeAttachment().getFilename(), getStampedActe(acte, null, null, currentLocalAuthority));
        }
        return zipGeneratorUtil.createZip(pdfs);
    }

    public byte[] getACKPdfs(ActeUuidsAndSearchUI acteUuidsAndSearchUI, String language)
            throws IOException, DocumentException {
        ITextRenderer renderer = new ITextRenderer();
        List<Acte> actes = getActesFromUuidsOrSearch(acteUuidsAndSearchUI);
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        for (Acte acte: actes) {
            if (acte.getLastHistoryStatus().equals(StatusType.ACK_RECEIVED)) {
                Map<String, String> mapString = extractDataFromActe(acte);

                Map<String, String> data = getTranslatedFieldsAndValues(mapString, language);
                String content = pdfGeneratorUtil.getContentPage("acte", data);

                renderer.setDocumentFromString(content);
                renderer.layout();

                // init of none unique page pdf
                if (renderer.getWriter() == null && actes.size() > 1) {
                    renderer.createPDF(os, false);
                }

                //none unique page need to declare NextDocument
                if (renderer.getWriter().getCurrentPageNumber() > 0) {
                    renderer.writeNextDocument();
                }
            }

        }


        try {
            if (renderer.getWriter().getCurrentPageNumber() == 1) {
                renderer.createPDF(os);
            } else {
                renderer.finishPDF();
            }
            byte[] pdf = os.toByteArray();
            return pdf;
        } catch (Exception e) {
            throw new NoContentException();
        } finally {
            os.close();
        }

    }


    private Map<String, String> extractDataFromActe(Acte acte){
        String shortEuropeanDatePattern = "dd/MM/yyyy";
        DateTimeFormatter shortEuropeanDateFormatter = DateTimeFormatter.ofPattern(shortEuropeanDatePattern);

        String longEuropeanDatePattern = "dd/MM/yyyy HH:mm:ss";
        DateTimeFormatter longEuropeanDateFormatter = DateTimeFormatter.ofPattern(longEuropeanDatePattern);


        Map<String, String> map = new HashMap<String, String>() {
            {
                put("status", acte.getActeHistories().last().getStatus().toString());
                put("number", acte.getNumber());
                put("uniqueId", acte.getMiatId());
                put("decision", acte.getDecision().format(shortEuropeanDateFormatter));
                put("nature", acte.getNature().toString());
                put("code", acte.getCode() +
                        (StringUtils.isNotBlank(acte.getCodeLabel()) ? " (" + acte.getCodeLabel() + ")" : ""));
                put("objet", acte.getObjet());
                put("acteAttachment", acte.getActeAttachment().getFilename());
                put("localAuthority", acte.getLocalAuthority().getName());
            }
        };

        for(ActeHistory acteHistory : acte.getActeHistories()){
            switch (acteHistory.getStatus()) {
                case SENT:
                    map.put("sentDate", acteHistory.getDate().format(longEuropeanDateFormatter));
                    break;
                case CANCELLATION_ASKED:
                    map.put("cancellationAskedDate", acteHistory.getDate().format(longEuropeanDateFormatter));
                    break;
                case CANCELLED:
                    map.put("cancelledDate", acteHistory.getDate().format(longEuropeanDateFormatter));
                    break;
                case ACK_RECEIVED:
                    map.put("ackReceivedDate", acteHistory.getDate().format(longEuropeanDateFormatter));
                    break;
                default:
                    break;
            }
        }

        try {
            JsonNode node = externalRestService.getProfile(acte.getProfileUuid());
            String applicantName = node.get("agent").get("given_name").asText() + " " +node.get("agent").get("family_name").asText();
            map.put("applicantName",  applicantName);
        }catch(Exception e){
             LOGGER.error("Agent not found ",e);
             return map;
        }

        return map;
    }

    // Doubles up a map into a new map with for each entry: ("entry_value", value)
    // and ("entry_fieldName", translate(entry_fieldName))
    private Map<String, String> getTranslatedFieldsAndValues(Map<String, String> map, String language) {
        if (StringUtils.isBlank(language))
            language = "fr";
        ClassPathResource classPathResource = new ClassPathResource("/locales/" + language + "/acte.json");

        // First part of the map with mandatory ("entry_value", value)
        Map<String, String> data = new HashMap<String, String>() {
            {
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    put(entry.getKey() + "_value", entry.getValue());
                }
            }
        };
        try {
            // If we can translate we add ("entry_fieldName", translate(entry_fieldName))
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(classPathResource.getInputStream());
            for (Map.Entry<String, String> entry : map.entrySet()) {
                // TODO: Hack, fix me !
                if (entry.getKey().equals("status"))
                    data.put(entry.getKey() + "_value",
                            jsonNode.get("acte").get("nature").get("AR_PREF").asText());
                else
                    data.put(entry.getKey() + "_fieldName",
                            jsonNode.get("acte").get("fields").get(entry.getKey()).asText());
            }
        } catch (Exception e) {
            // else no translation
            LOGGER.error("Error while parsing json translations: {}", e);
            for (Map.Entry<String, String> entry : map.entrySet())
                data.put(entry.getKey() + "_fieldName", entry.getKey());
        }
        return data;
    }

    public String getActeHistoryDefinition(ActeHistory acteHistory) {
        return localesService.getMessage("fr", "acte", "$.acte.status." + acteHistory.getStatus().name());
    }

    public List<String> getActeHistoryDefinitions(ActeHistory acteHistory) {
        return Arrays.asList(generateMiatId(getByUuid(acteHistory.getActeUuid())),
                getActeHistoryDefinition(acteHistory));
    }

    public String generateMiatId(Acte acte) {
        return archiveService.generateMiatId(acte);
    }

    public byte[] getStampedActe(Acte acte, Integer x, Integer y, LocalAuthority localAuthority)
            throws IOException, DocumentException {
        PdfReader pdfReader = new PdfReader(acte.getActeAttachment().getFile());
        if (x == null || y == null) {
            if(pdfGeneratorUtil.pdfIsRotated(pdfReader)){
                //landscape case
                y = localAuthority.getStampPosition().getX();
                x = localAuthority.getStampPosition().getY();
            }else{
                //portrait case
                x = localAuthority.getStampPosition().getX();
                y = localAuthority.getStampPosition().getY();
            }
        }
        ActeHistory ackHistory = acte.getActeHistories().stream()
                .filter(acteHistory -> acteHistory.getStatus().equals(StatusType.ACK_RECEIVED)).findFirst().get();
        return pdfGeneratorUtil.stampPDF(generateMiatId(acte),
                ackHistory.getDate().format(DateTimeFormatter.ofPattern("dd/MM/YYYY")),
                acte.getActeAttachment().getFile(), x, y);
    }

    public byte[] getStampedAnnexe(Acte acte, Attachment attachment, Integer x, Integer y,
            LocalAuthority localAuthority) throws IOException, DocumentException {
        if (x == null || y == null) {
            x = localAuthority.getStampPosition().getX();
            y = localAuthority.getStampPosition().getY();
        }
        ActeHistory ackHistory = acte.getActeHistories().stream()
                .filter(acteHistory -> acteHistory.getStatus().equals(StatusType.ACK_RECEIVED)).findFirst().get();
        return pdfGeneratorUtil.stampPDF(generateMiatId(acte),
                ackHistory.getDate().format(DateTimeFormatter.ofPattern("dd/MM/YYYY")), attachment.getFile(), x, y);
    }

    public Thumbnail getActeAttachmentThumbnail(String uuid) throws IOException {
        byte[] pdf = getByUuid(uuid).getActeAttachment().getFile();
        return pdfGeneratorUtil.getPDFThumbnail(pdf);
    }

    public Optional<Acte> getFirstActeCreatedForNature(ActeNature nature, String uuid, Boolean isPublicWebsite) {
        return acteRepository.findFirstByNatureAndLocalAuthorityUuidAndIsPublicWebsiteOrderByDecisionAsc(nature, uuid,
                isPublicWebsite);
    }

    public void askAllNomenclature() {
        List<LocalAuthority> localAuthorities = localAuthorityService.getAll();
        localAuthorities.forEach(localAuthority -> askNomenclature(localAuthority, false));
    }

    public List<Acte> getActesMatchingMiatId(String str) {
        return acteRepository.findAllByDraftNullAndMiatIdContainingIgnoreCase(str);
    }

    public HttpStatus askNomenclature(LocalAuthority localAuthority, boolean force) {
        Attachment attachment = archiveService.createNomenclatureAskMessage(localAuthority, force);
        try {
            LOGGER.info((force ? "FORCING" : "Asking") + " a new classification for localAuthority {}",
                    localAuthority.getUuid());
            return send(attachment.getFile(), attachment.getFilename());
        } catch (Exception e) {
            LOGGER.error("Error while asking a new classification for localAuthority {}: {}", localAuthority.getUuid(),
                    e);
            return HttpStatus.BAD_REQUEST;
        }
    }

    public HttpStatus send(byte[] file, String fileName) {

        System.setProperty("javax.net.debug", "all");

        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();

        map.add("name", fileName);
        map.add("filename", fileName);

        ByteArrayResource contentsAsResource = new ByteArrayResource(file) {
            @Override
            public String getFilename() {
                return fileName; // Filename has to be returned in order to be able to post.
            }
        };

        map.add("file", contentsAsResource);
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Agent", "stela");
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<LinkedMultiValueMap<String, Object>>(
                map, headers);

        HttpStatus httpStatus = null;
        try {
            ResponseEntity<String> result = miatRestTemplate.exchange(acteUrl, HttpMethod.POST, requestEntity,
                    String.class);
            httpStatus = result.getStatusCode();
        } catch (ResourceAccessException e) {
            LOGGER.error("Miat rescue server unavailable: {}", e.getMessage());
            httpStatus = HttpStatus.NOT_FOUND;
        } catch (Exception e) {
            LOGGER.error("Miat main server unavailable (might be an error on our side): {}", e.getMessage());
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        if (!HttpStatus.OK.equals(httpStatus)) {
            try {
                ResponseEntity<String> result = miatRestTemplate.exchange(rescueUrl, HttpMethod.POST, requestEntity,
                        String.class);
                httpStatus = result.getStatusCode();
            } catch (ResourceAccessException e) {
                LOGGER.error("Miat rescue server unavailable: {}", e.getMessage());
                httpStatus = HttpStatus.NOT_FOUND;
            } catch (Exception e) {
                LOGGER.error("Miat rescue server unavailable (might be an error on our side): {}", e.getMessage());
                httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            }
        }
        return httpStatus;
    }

    public Optional<ActeHistory> findFirstActeHistory(Acte acte, StatusType statusType) {
        return acte.getActeHistories().stream().filter(acteHistory -> statusType.equals(acteHistory.getStatus()))
                .findFirst();
    }

    public Stream<ActeHistory> streamActeHistoriesByStatus(Acte acte, StatusType statusType) {
        return acte.getActeHistories().stream().filter(acteHistory -> statusType.equals(acteHistory.getStatus()));
    }

    public boolean numberExist(String number, String localAuthorityUuid) {
        return !acteRepository.findByNumberAndLocalAuthorityUuid(number, localAuthorityUuid).isEmpty();
    }

    @Override
    public void onApplicationEvent(@NotNull ActeHistoryEvent event) {
        ActeHistory history = event.getActeHistory();
        Acte acte = getByUuid(history.getActeUuid());
        acte.getActeHistories().add(history);
        if (history.getStatus() != StatusType.NOTIFICATION_SENT && history.getStatus() != StatusType.GROUP_NOTIFICATION_SENT) {
            acte.setLastHistoryStatus(history.getStatus());
            acte.setLastHistoryDate(history.getDate());
            acte.setLastHistoryFlux(history.getFlux());
        }

        acteRepository.save(acte);
    }

}

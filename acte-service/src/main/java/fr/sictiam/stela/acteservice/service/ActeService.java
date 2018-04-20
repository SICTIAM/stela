package fr.sictiam.stela.acteservice.service;

import com.lowagie.text.DocumentException;
import fr.sictiam.stela.acteservice.dao.ActeHistoryRepository;
import fr.sictiam.stela.acteservice.dao.ActeRepository;
import fr.sictiam.stela.acteservice.dao.AttachmentRepository;
import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.model.ActeHistory;
import fr.sictiam.stela.acteservice.model.ActeNature;
import fr.sictiam.stela.acteservice.model.Attachment;
import fr.sictiam.stela.acteservice.model.Flux;
import fr.sictiam.stela.acteservice.model.LocalAuthority;
import fr.sictiam.stela.acteservice.model.StatusType;
import fr.sictiam.stela.acteservice.model.event.ActeHistoryEvent;
import fr.sictiam.stela.acteservice.model.ui.ActeCSVUI;
import fr.sictiam.stela.acteservice.model.ui.ActeUuidsAndSearchUI;
import fr.sictiam.stela.acteservice.service.exceptions.ActeNotFoundException;
import fr.sictiam.stela.acteservice.service.exceptions.ActeNotSentException;
import fr.sictiam.stela.acteservice.service.exceptions.CancelForbiddenException;
import fr.sictiam.stela.acteservice.service.exceptions.FileNotFoundException;
import fr.sictiam.stela.acteservice.service.exceptions.HistoryNotFoundException;
import fr.sictiam.stela.acteservice.service.exceptions.NoContentException;
import fr.sictiam.stela.acteservice.service.util.PdfGeneratorUtil;
import fr.sictiam.stela.acteservice.service.util.ZipGeneratorUtil;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONObject;
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
import org.springframework.util.FileCopyUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.validation.constraints.NotNull;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import java.util.stream.Collectors;

@Service
public class ActeService implements ApplicationListener<ActeHistoryEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActeService.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final ActeRepository acteRepository;
    private final ActeHistoryRepository acteHistoryRepository;
    private final AttachmentRepository attachmentRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final LocalAuthorityService localAuthorityService;
    private final LocalesService localesService;
    private final ArchiveService archiveService;
    private final PdfGeneratorUtil pdfGeneratorUtil;
    private final ZipGeneratorUtil zipGeneratorUtil;

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
            PdfGeneratorUtil pdfGeneratorUtil, ZipGeneratorUtil zipGeneratorUtil, LocalesService localesService) {
        this.acteRepository = acteRepository;
        this.acteHistoryRepository = acteHistoryRepository;
        this.attachmentRepository = attachmentRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.localAuthorityService = localAuthorityService;
        this.archiveService = archiveService;
        this.pdfGeneratorUtil = pdfGeneratorUtil;
        this.zipGeneratorUtil = zipGeneratorUtil;
        this.localesService = localesService;
    }

    public Acte create(LocalAuthority currentLocalAuthority, Acte acte, MultipartFile file, MultipartFile... annexes)
            throws ActeNotSentException, IOException {
        acte.setActeAttachment(new Attachment(file.getBytes(), file.getOriginalFilename(), file.getSize()));
        List<Attachment> transformedAnnexes = new ArrayList<>();
        for (MultipartFile annexe : annexes) {
            transformedAnnexes.add(new Attachment(annexe.getBytes(), annexe.getOriginalFilename(), annexe.getSize()));
        }
        acte.setAnnexes(transformedAnnexes);
        acte.setCreation(LocalDateTime.now());
        acte.setLocalAuthority(currentLocalAuthority);
        acte.setCodeLabel(localAuthorityService.getCodeMatiereLabel(currentLocalAuthority.getUuid(), acte.getCode()));

        if (!currentLocalAuthority.getCanPublishWebSite())
            acte.setPublicWebsite(false);
        if (!currentLocalAuthority.getCanPublishRegistre())
            acte.setPublic(false);

        return publishActe(acte);
    }

    public Acte publishActe(Acte acte) {
        Acte created = acteRepository.save(acte);

        ActeHistory acteHistory = new ActeHistory(acte.getUuid(), StatusType.CREATED, Flux.TRANSMISSION_ACTE);
        applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory));

        LOGGER.info("Acte {} created with id {}", created.getNumber(), created.getUuid());

        return created;
    }

    public Acte findByIdActe(String iDActe) {
        String[] idActeSplit = iDActe.split("-");
        String siren = idActeSplit[1];
        String number = idActeSplit[3];
        return acteRepository.findByNumberAndLocalAuthoritySiren(number, siren).orElseThrow(ActeNotFoundException::new);
    }

    public Acte receiveAREvent(String iDActe, StatusType statusType) {
        Acte acte = findByIdActe(iDActe);
        ActeHistory acteHistory = new ActeHistory(acte.getUuid(), statusType);
        applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory));
        LOGGER.info("Acte {} {} with id {}", acte.getNumber(), statusType, acte.getUuid());
        return acte;
    }

    public void receiveAnomalie(String siren, String number, String detail) {
        Acte acte = acteRepository.findByNumberAndLocalAuthoritySiren(number, siren)
                .orElseThrow(ActeNotFoundException::new);
        ActeHistory acteHistory = new ActeHistory(acte.getUuid(), StatusType.NACK_RECEIVED, detail);
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

        List<Predicate> predicates = getQueryPredicates(builder, acteRoot, multifield, number, objet, nature,
                decisionFrom, decisionTo, status, currentLocalAuthUuid, groups);
        query.select(builder.count(acteRoot));
        query.where(predicates.toArray(new Predicate[predicates.size()]));

        return entityManager.createQuery(query).getSingleResult();
    }

    public List<Acte> getAllWithQuery(String multifield, String number, String objet, ActeNature nature,
            LocalDate decisionFrom, LocalDate decisionTo, StatusType status, Integer limit, Integer offset,
            String column, String direction, String currentLocalAuthUuid, Set<String> groups) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Acte> query = builder.createQuery(Acte.class);
        Root<Acte> acteRoot = query.from(Acte.class);

        String columnAttribute = StringUtils.isEmpty(column) ? "creation" : column;
        List<Predicate> predicates = getQueryPredicates(builder, acteRoot, multifield, number, objet, nature,
                decisionFrom, decisionTo, status, currentLocalAuthUuid, groups);
        query.where(predicates.toArray(new Predicate[predicates.size()]))
                .orderBy(!StringUtils.isEmpty(direction) && direction.equals("ASC")
                        ? builder.asc(acteRoot.get(columnAttribute))
                        : builder.desc(acteRoot.get(columnAttribute)));

        return entityManager.createQuery(query).setFirstResult(offset).setMaxResults(limit).getResultList();
    }

    private List<Predicate> getQueryPredicates(CriteriaBuilder builder, Root<Acte> acteRoot, String multifield,
            String number, String objet, ActeNature nature, LocalDate decisionFrom, LocalDate decisionTo,
            StatusType status, String currentLocalAuthUuid, Set<String> groups) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(builder.and(builder.isNull(acteRoot.get("draft"))));
        if (StringUtils.isNotBlank(multifield)) {
            predicates.add(builder.or(
                    builder.like(builder.lower(acteRoot.get("number")), "%" + multifield.toLowerCase() + "%"),
                    builder.like(builder.lower(acteRoot.get("objet")), "%" + multifield.toLowerCase() + "%")));
        }
        if (StringUtils.isNotBlank(number))
            predicates.add(
                    builder.and(builder.like(builder.lower(acteRoot.get("number")), "%" + number.toLowerCase() + "%")));
        if (StringUtils.isNotBlank(objet))
            predicates.add(
                    builder.and(builder.like(builder.lower(acteRoot.get("objet")), "%" + objet.toLowerCase() + "%")));
        if (StringUtils.isNotBlank(currentLocalAuthUuid)) {
            Join<LocalAuthority, Acte> LocalAuthorityJoin = acteRoot.join("localAuthority");
            LocalAuthorityJoin.on(builder.equal(LocalAuthorityJoin.get("uuid"), currentLocalAuthUuid));
        }
        if (!CollectionUtils.isEmpty(groups)) {
            predicates.add(builder.or(acteRoot.get("groupUuid").in(groups),
                    builder.equal(acteRoot.get("groupUuid"), ""), builder.isNull(acteRoot.get("groupUuid"))));
        } else {
            predicates.add(builder.or(builder.equal(acteRoot.get("groupUuid"), ""),
                    builder.isNull(acteRoot.get("groupUuid"))));
        }

        if (nature != null)
            predicates.add(builder.and(builder.equal(acteRoot.get("nature"), nature)));
        if (decisionFrom != null && decisionTo != null)
            predicates.add(builder.and(builder.between(acteRoot.get("decision"), decisionFrom, decisionTo)));

        if (status != null) {
            // TODO: Find a way to do a self left join using a CriteriaQuery instead of a
            // native one
            Query q = entityManager.createNativeQuery(
                    "select ah1.acte_uuid from acte_history ah1 left join acte_history ah2 on (ah1.acte_uuid = ah2.acte_uuid and ah1.date < ah2.date) where ah2.date is null and ah1.status = '"
                            + status + "'");
            List<String> acteHistoriesActeUuids = q.getResultList();
            if (acteHistoriesActeUuids.size() > 0)
                predicates.add(builder.and(acteRoot.get("uuid").in(acteHistoriesActeUuids)));
            else
                predicates.add(builder.and(acteRoot.get("uuid").isNull()));
        }

        return predicates;
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
        query.where(mainQueryPredicates.toArray(new Predicate[] {}));
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
            JSONObject jsonObject = new JSONObject(
                    new String(FileCopyUtils.copyToByteArray(classPathResource.getInputStream())));
            for (Acte acte : actes) {
                acteCSVUIs.add(new ActeCSVUI(acte.getNumber(), acte.getObjet(), acte.getDecision().toString(),
                        jsonObject.getJSONObject("acte").getJSONObject("nature").getString(acte.getNature().toString()),
                        jsonObject.getJSONObject("acte").getJSONObject("status")
                                .getString(acte.getActeHistories().last().getStatus().toString())));
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
            JSONObject jsonObject = new JSONObject(
                    new String(FileCopyUtils.copyToByteArray(classPathResource.getInputStream())));
            List<String> translatedList = new ArrayList<>();
            for (String field : fields)
                translatedList.add(jsonObject.getJSONObject("acte").getJSONObject("fields").getString(field));
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
        List<String> pages = new ArrayList<>();
        List<Acte> actes = getActesFromUuidsOrSearch(acteUuidsAndSearchUI);
        for (Acte acte : actes) {
            if (acte.getActeHistories().last().getStatus().equals(StatusType.ACK_RECEIVED)) {
                Map<String, String> mapString = new HashMap<String, String>() {
                    {
                        put("status", acte.getActeHistories().last().getStatus().toString());
                        put("number", acte.getNumber());
                        put("decision", acte.getDecision().toString());
                        put("nature", acte.getNature().toString());
                        put("code", acte.getCode() + " (" + acte.getCodeLabel() + ")");
                        put("objet", acte.getObjet());
                        put("acteAttachment", acte.getActeAttachment().getFilename());
                    }
                };
                Map<String, String> data = getTranslatedFieldsAndValues(mapString, language);
                pages.add(pdfGeneratorUtil.getContentPage("acte", data));
            }
        }
        if (pages.size() == 0)
            throw new NoContentException();
        return pdfGeneratorUtil.createPdf(pages);
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
            JSONObject jsonObject = new JSONObject(
                    new String(FileCopyUtils.copyToByteArray(classPathResource.getInputStream())));
            for (Map.Entry<String, String> entry : map.entrySet()) {
                // TODO: Hack, fix me !
                if (entry.getKey().equals("status"))
                    data.put(entry.getKey() + "_value",
                            jsonObject.getJSONObject("acte").getJSONObject("status").getString(entry.getValue()));
                else
                    data.put(entry.getKey() + "_fieldName",
                            jsonObject.getJSONObject("acte").getJSONObject("fields").getString(entry.getKey()));
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
        if (x == null || y == null) {
            x = localAuthority.getStampPosition().getX();
            y = localAuthority.getStampPosition().getY();
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

    public byte[] getActeAttachmentThumbnail(String uuid) throws IOException {
        byte[] pdf = getByUuid(uuid).getActeAttachment().getFile();
        return pdfGeneratorUtil.getPDFThumbnail(pdf);
    }

    public Optional<Acte> getStartYear(ActeNature nature, String uuid, Boolean isPublicWebsite) {
        return acteRepository.findFirstByNatureAndLocalAuthorityUuidAndIsPublicWebsiteOrderByDecisionAsc(nature, uuid,
                isPublicWebsite);
    }

    public void askAllNomenclature() {
        List<LocalAuthority> localAuthorities = localAuthorityService.getAll();
        localAuthorities.forEach(this::askNomenclature);
    }

    public List<Acte> getActeMacth(String str) {
        return acteRepository.findAllByDraftNullAndMiatIdContainingIgnoreCase(str);
    }

    public HttpStatus askNomenclature(LocalAuthority localAuthority) {
        Attachment attachment = archiveService.createNomenclatureAskMessage(localAuthority);
        try {
            return send(attachment.getFile(), attachment.getFilename());
        } catch (Exception e) {
            LOGGER.error("Error while asking a new classification for localAuthority {}: {}", localAuthority.getUuid(),
                    e);
            return HttpStatus.INTERNAL_SERVER_ERROR;
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
        } catch (Exception e) {
            LOGGER.error("Miat main server unavailable!");
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        if (!HttpStatus.OK.equals(httpStatus)) {
            try {
                ResponseEntity<String> result = miatRestTemplate.exchange(rescueUrl, HttpMethod.POST, requestEntity,
                        String.class);
                httpStatus = result.getStatusCode();
            } catch (Exception e) {
                LOGGER.error("Miat rescue server unavailable!");
                httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            }
        }
        return httpStatus;
    }

    @Override
    public void onApplicationEvent(@NotNull ActeHistoryEvent event) {
        Acte acte = getByUuid(event.getActeHistory().getActeUuid());
        acte.getActeHistories().add(event.getActeHistory());
        acteRepository.save(acte);
    }
}

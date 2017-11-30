package fr.sictiam.stela.acteservice.service;

import fr.sictiam.stela.acteservice.dao.ActeHistoryRepository;
import fr.sictiam.stela.acteservice.dao.ActeRepository;
import fr.sictiam.stela.acteservice.dao.AttachmentRepository;
import fr.sictiam.stela.acteservice.model.*;
import fr.sictiam.stela.acteservice.model.event.ActeHistoryEvent;
import fr.sictiam.stela.acteservice.model.ui.ActeCSVUI;
import fr.sictiam.stela.acteservice.model.ui.ActeUuidsAndSearchUI;
import fr.sictiam.stela.acteservice.service.exceptions.*;
import fr.sictiam.stela.acteservice.service.util.PdfGeneratorUtil;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
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
    private final PdfGeneratorUtil pdfGeneratorUtil;

    @Autowired
    public ActeService(ActeRepository acteRepository, ActeHistoryRepository acteHistoryRepository,
                       AttachmentRepository attachmentRepository, ApplicationEventPublisher applicationEventPublisher,
                       LocalAuthorityService localAuthorityService, PdfGeneratorUtil pdfGeneratorUtil) {
        this.acteRepository = acteRepository;
        this.acteHistoryRepository = acteHistoryRepository;
        this.attachmentRepository = attachmentRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.localAuthorityService = localAuthorityService;
        this.pdfGeneratorUtil = pdfGeneratorUtil;
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
        acte.setActeAttachment(new Attachment(file.getBytes(), file.getOriginalFilename(), file.getSize()));
        List<Attachment> transformedAnnexes = new ArrayList<>();
        for (MultipartFile annexe: annexes) {
            transformedAnnexes.add(new Attachment(annexe.getBytes(), annexe.getOriginalFilename(), annexe.getSize()));
        }
        acte.setAnnexes(transformedAnnexes);
        acte.setCreation(LocalDateTime.now());
        acte.setLocalAuthority(currentLocalAuthority);
        acte.setCodeLabel(localAuthorityService.getCodeMatiereLabel(currentLocalAuthority.getUuid(), acte.getCode()));

        if(!currentLocalAuthority.getCanPublishWebSite()) acte.setPublicWebsite(false);
        if(!currentLocalAuthority.getCanPublishRegistre()) acte.setPublic(false);

        Acte created = acteRepository.save(acte);

        ActeHistory acteHistory = new ActeHistory(acte.getUuid(), StatusType.CREATED);
        applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory));

        LOGGER.info("Acte {} created with id {}", created.getNumber(), created.getUuid());

        return created;
    }

    public Acte sendDraft(String uuid) {
        Acte acte = getDraftByUuid(uuid);
        // TODO: Do some backend validations on the acte

        acte.setDraft(false);
        acte.setCreation(LocalDateTime.now());
        Acte created = acteRepository.save(acte);

        ActeHistory acteHistory = new ActeHistory(acte.getUuid(), StatusType.CREATED);
        applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory));
        LOGGER.info("Acte {} created with id {}", created.getNumber(), created.getUuid());

        return created;
    }
    
    public Acte receiveARActe(String number) {
        Acte acte = acteRepository.findByNumber(number).get();
        ActeHistory acteHistory = new ActeHistory(acte.getUuid(), StatusType.ACK_RECEIVED);
        applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory));
        LOGGER.info("Acte {} AR with id {}", acte.getNumber(), acte.getUuid());
        return acte;
    }

    public Acte saveDraft(Acte acte, LocalAuthority currentLocalAuthority) {
        acte.setCreation(LocalDateTime.now()); // Hack: Used as 'lastUpdated'
        acte.setLocalAuthority(currentLocalAuthority);
        acte.setCodeLabel(localAuthorityService.getCodeMatiereLabel(currentLocalAuthority.getUuid(), acte.getCode()));
        if(!currentLocalAuthority.getCanPublishWebSite()) acte.setPublicWebsite(false);
        if(!currentLocalAuthority.getCanPublishRegistre()) acte.setPublic(false);
        acte.setDraft(true);
        return acteRepository.save(acte);
    }

    public void closeDraft(Acte acte, LocalAuthority currentLocalAuthority) {
        if(acte.empty()) acteRepository.delete(acte);
        else saveDraft(acte, currentLocalAuthority);
    }

    public Acte saveDraftFile(String uuid, MultipartFile file, LocalAuthority currentLocalAuthority) throws IOException {
        Acte acte = StringUtils.isBlank(uuid) ? new Acte() : getDraftByUuid(uuid);
        acte.setActeAttachment(new Attachment(file.getBytes(), file.getOriginalFilename(), file.getSize()));
        return saveDraft(acte, currentLocalAuthority);
    }

    public Acte saveDraftAnnexe(String uuid, MultipartFile file, LocalAuthority currentLocalAuthority) throws IOException {
        Acte acte = StringUtils.isBlank(uuid) ? new Acte() : getDraftByUuid(uuid);
        List<Attachment> annexes = acte.getAnnexes();
        annexes.add(new Attachment(file.getBytes(), file.getOriginalFilename(), file.getSize()));
        acte.setAnnexes(annexes);
        return saveDraft(acte, currentLocalAuthority);
    }

    public void deleteDraftAnnexe(String acteUuid, String uuid) {
        Acte acte = getDraftByUuid(acteUuid);
        if(acte.getAnnexes().stream().anyMatch( attachment -> attachment.getUuid().equals(uuid))) {
            List<Attachment> annexes = acte.getAnnexes().stream().filter(attachment -> !attachment.getUuid().equals(uuid)).collect(Collectors.toList());
            acte.setAnnexes(annexes);
            acteRepository.save(acte);
            attachmentRepository.delete(attachmentRepository.findByUuid(uuid).get());
        }
    }

    public void deleteDraftFile(String uuid) {
        Acte acte = getDraftByUuid(uuid);
        Attachment file = acte.getActeAttachment();
        if(file != null) {
            acte.setActeAttachment(null);
            acteRepository.save(acte);
            attachmentRepository.delete(file);
        }
    }

    public List<Acte> getAllWithQuery(String number, String objet, ActeNature nature, LocalDate decisionFrom, LocalDate decisionTo, StatusType status) {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Acte> query = builder.createQuery(Acte.class);
        Root<Acte> acteRoot = query.from(Acte.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(builder.and(builder.equal(acteRoot.get("draft"), false)));
        if(StringUtils.isNotBlank(number)) predicates.add(builder.and(builder.like(builder.lower(acteRoot.get("number")), "%"+number.toLowerCase()+"%")));
        if(StringUtils.isNotBlank(objet)) predicates.add(builder.and(builder.like(builder.lower(acteRoot.get("objet")), "%"+objet.toLowerCase()+"%")));
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

    public List<Acte> getDrafts() {
        return acteRepository.findAllByDraftTrueOrderByCreationDesc();
    }

    public void deleteDrafts(List<String> uuids) {
        List<Acte> drafts = getDraftsFromUuids(uuids);
        drafts.forEach(acteRepository::delete);
    }

    public Acte getDraftByUuid(String uuid) {
        return acteRepository.findByUuidAndDraftTrue(uuid).orElseThrow(ActeNotFoundException::new);
    }

    public Acte getByUuid(String uuid) {
        return acteRepository.findByUuidAndDraftFalse(uuid).orElseThrow(ActeNotFoundException::new);
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
    
    public void sent(String acteUuid) {
        ActeHistory acteHistory = new ActeHistory(acteUuid, StatusType.SENT);
        applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory));       
    }
    
    public void notSent(String acteUuid) {
        ActeHistory acteHistory = new ActeHistory(acteUuid, StatusType.NOT_SENT);
        applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory));       
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

    public List<ActeCSVUI> getActesCSV(ActeUuidsAndSearchUI acteUuidsAndSearchUI, String language) {
        if(StringUtils.isBlank(language)) language = "fr";
        ClassPathResource classPathResource = new ClassPathResource("/locales/" + language + "/acte.json");
        List<Acte> actes = getActesFromUuidsOrSearch(acteUuidsAndSearchUI);
        List<ActeCSVUI> acteCSVUIs = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(new String(FileCopyUtils.copyToByteArray(classPathResource.getInputStream())));
            for(Acte acte : actes) {
                acteCSVUIs.add(new ActeCSVUI(
                        acte.getNumber(),
                        acte.getObjet(),
                        acte.getDecision().toString(),
                        jsonObject.getJSONObject("acte").getJSONObject("nature").getString(acte.getNature().toString()),
                        jsonObject.getJSONObject("acte").getJSONObject("status").getString(acte.getActeHistories().last().getStatus().toString())));
            }
        } catch (Exception e) {
            LOGGER.error("Error while trying to translate Acte values, will take untranslated values: {}", e);
            for(Acte acte : actes) {
                acteCSVUIs.add(new ActeCSVUI(
                        acte.getNumber(),
                        acte.getObjet(),
                        acte.getDecision().toString(),
                        acte.getNature().toString(),
                        acte.getActeHistories().last().getStatus().toString()));
            }
        }
        if(acteCSVUIs.size() == 0) throw new NoContentException();
        return acteCSVUIs;
    }

    public List<String> getTranslatedCSVFields(List<String> fields, String language) {
        if(StringUtils.isBlank(language)) language = "fr";
        ClassPathResource classPathResource = new ClassPathResource("/locales/" + language + "/acte.json");
        try {
            JSONObject jsonObject = new JSONObject(new String(FileCopyUtils.copyToByteArray(classPathResource.getInputStream())));
            List<String> translatedList = new ArrayList<>();
            for(String field: fields) translatedList.add(jsonObject.getJSONObject("acte").getJSONObject("fields").getString(field));
            return translatedList;
        } catch (Exception e) {
            LOGGER.error("Error while trying to translate CSV fields, will take untranslated fields: {}", e);
            return fields;
        }
    }

    private List<Acte> getActesFromUuidsOrSearch(ActeUuidsAndSearchUI ui) {
        return ui.getUuids().size() > 0 ?
                ui.getUuids().stream().map(this::getByUuid).collect(Collectors.toList()) :
                getAllWithQuery(ui.getNumber(), ui.getObjet(), ui.getNature(), ui.getDecisionFrom(), ui.getDecisionTo(), ui.getStatus());
    }

    private List<Acte> getDraftsFromUuids(List<String> uuids) {
        return uuids.size() > 0 ?
                uuids.stream().map(this::getDraftByUuid).collect(Collectors.toList()) :
                acteRepository.findAllByDraftTrueOrderByCreationDesc();
    }

    public byte[] getACKPdfs(ActeUuidsAndSearchUI acteUuidsAndSearchUI, String language) throws Exception {
        List<String> pages = new ArrayList<>();
        List<Acte> actes = getActesFromUuidsOrSearch(acteUuidsAndSearchUI);
        for (Acte acte: actes) {
            if(acte.getActeHistories().last().getStatus().equals(StatusType.ACK_RECEIVED)) {
                Map<String,String> mapString = new HashMap<String, String>() {{
                    put("status", acte.getActeHistories().last().getStatus().toString());
                    put("number", acte.getNumber());
                    put("decision", acte.getDecision().toString());
                    put("nature", acte.getNature().toString());
                    put("code", acte.getCode() + " (" + acte.getCodeLabel() +")");
                    put("objet", acte.getObjet());
                    put("filename", acte.getActeAttachment().getFilename());
                }};
                Map<String,String> data = getTranslatedFieldsAndValues(mapString, language);
                pages.add(pdfGeneratorUtil.getContentPage("acte", data));
            }
        }
        if(pages.size() == 0) throw new NoContentException();
        return pdfGeneratorUtil.createPdf(pages);
    }

    // Doubles up a map into a new map with for each entry: ("entry_value", value) and ("entry_fieldName", translate(entry_fieldName))
    private Map<String, String> getTranslatedFieldsAndValues(Map<String, String> map, String language) {
        if(StringUtils.isBlank(language)) language = "fr";
        ClassPathResource classPathResource = new ClassPathResource("/locales/" + language + "/acte.json");

        // First part of the map with mandatory ("entry_value", value)
        Map<String,String> data = new HashMap<String, String>() {{
            for (Map.Entry<String, String> entry : map.entrySet()) {
                put(entry.getKey()+"_value", entry.getValue());
            }
        }};
        try {
            // If we can translate we add ("entry_fieldName", translate(entry_fieldName))
            JSONObject jsonObject = new JSONObject(new String(FileCopyUtils.copyToByteArray(classPathResource.getInputStream())));
            for (Map.Entry<String, String> entry : map.entrySet()){
                // TODO: Hack, fix me !
                if(entry.getKey().equals("status")) data.put(entry.getKey()+"_value", jsonObject.getJSONObject("acte").getJSONObject("status").getString(entry.getValue()));
                else data.put(entry.getKey()+"_fieldName", jsonObject.getJSONObject("acte").getJSONObject("fields").getString(entry.getKey()));
            }
        } catch (Exception e) {
            // else no translation
            LOGGER.error("Error while parsing json translations: {}", e);
            for (Map.Entry<String, String> entry : map.entrySet())
                data.put(entry.getKey()+"_fieldName", entry.getKey());
        }
        return data;
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

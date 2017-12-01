package fr.sictiam.stela.acteservice.service;

import fr.sictiam.stela.acteservice.dao.ActeDraftRepository;
import fr.sictiam.stela.acteservice.dao.ActeRepository;
import fr.sictiam.stela.acteservice.dao.AttachmentRepository;
import fr.sictiam.stela.acteservice.model.*;
import fr.sictiam.stela.acteservice.model.event.ActeHistoryEvent;
import fr.sictiam.stela.acteservice.model.ui.ActeDraftUI;
import fr.sictiam.stela.acteservice.model.ui.CustomValidationUI;
import fr.sictiam.stela.acteservice.model.ui.DraftUI;
import fr.sictiam.stela.acteservice.service.exceptions.*;
import fr.sictiam.stela.acteservice.validation.ValidationUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.ObjectError;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DraftService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DraftService.class);

    private final ActeRepository acteRepository;
    private final ActeDraftRepository acteDraftRepository;
    private final AttachmentRepository attachmentRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final LocalAuthorityService localAuthorityService;

    @Autowired
    public DraftService(ActeRepository acteRepository, ActeDraftRepository acteDraftRepository,
                        AttachmentRepository attachmentRepository, ApplicationEventPublisher applicationEventPublisher,
                        LocalAuthorityService localAuthorityService) {
        this.acteRepository = acteRepository;
        this.acteDraftRepository = acteDraftRepository;
        this.attachmentRepository = attachmentRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.localAuthorityService = localAuthorityService;
    }

    public Acte submitActeDraft(Acte acte) {
        Draft draft = acte.getDraft();
        acte.setDraft(null);
        acte.setCreation(LocalDateTime.now());
        Acte created = acteRepository.save(acte);
        acteDraftRepository.delete(draft);

        ActeHistory acteHistory = new ActeHistory(acte.getUuid(), StatusType.CREATED);
        applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory));
        LOGGER.info("Acte {} created with id {}", created.getNumber(), created.getUuid());

        return created;
    }

    public Optional<CustomValidationUI> sumitDraft(String uuid) {
        List<Acte> actes = getActeDrafts(uuid);
        Draft draft = getDraftByUuid(uuid);

        CustomValidationUI customValidationUI = null;
        for (Acte acte:actes) {
            List<ObjectError> errors = ValidationUtil.validateActe(acte);
            if (!errors.isEmpty()) {
                if (customValidationUI == null) {
                    customValidationUI = new CustomValidationUI(errors, "has failed");
                } else {
                    customValidationUI.getErrors().addAll(errors);
                }
            }
        }
        if(customValidationUI != null) return Optional.of(customValidationUI);

        actes.forEach(acte -> {
            acte.setDraft(null);
            acte.setCreation(LocalDateTime.now());
            Acte created = acteRepository.save(acte);

            ActeHistory acteHistory = new ActeHistory(acte.getUuid(), StatusType.CREATED);
            applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory));
            LOGGER.info("Acte {} created with id {}", created.getNumber(), created.getUuid());
        });
        acteDraftRepository.delete(draft);
        return Optional.empty();
    }

    public void upddateDraftFields(DraftUI draftUI) {
        Draft draft = getDraftByUuid(draftUI.getUuid());
        draft.setDecision(draftUI.getDecision());
        draft.setNature(draftUI.getNature());
        updateLastModifiedDraft(draft.getUuid());
        acteDraftRepository.save(draft);
    }

    private Acte getEmptyActe(LocalAuthority currentLocalAuthority) {
        Acte acte = new Acte();
        acte.setLocalAuthority(currentLocalAuthority);
        acte.setCodeLabel(localAuthorityService.getCodeMatiereLabel(currentLocalAuthority.getUuid(), acte.getCode()));
        if(!currentLocalAuthority.getCanPublishWebSite()) acte.setPublicWebsite(false);
        if(!currentLocalAuthority.getCanPublishRegistre()) acte.setPublic(false);
        return acte;
    }

    public Acte newDraft(LocalAuthority currentLocalAuthority, ActeMode mode) {
        Acte acte = getEmptyActe(currentLocalAuthority);
        acte.setDraft(new Draft(LocalDateTime.now(), mode));
        return acteRepository.save(acte);
    }

    public DraftUI newBatchedDraft(LocalAuthority currentLocalAuthority) {
        Acte acte = newDraft(currentLocalAuthority, ActeMode.ACTE_BATCH);
        Draft draft = acte.getDraft();
        ActeDraftUI acteDraftUI = new ActeDraftUI(acte.getUuid(), acte.getNumber(), acte.getObjet(), acte.getNature());
        return new DraftUI(draft.getUuid(), Collections.singletonList(acteDraftUI), draft.getLastModified(), draft.getMode(), draft.getDecision(), draft.getNature());
    }

    @Transactional
    public ActeDraftUI newActeForDraft(String uuid, LocalAuthority currentLocalAuthority) {
        Draft draft = getDraftByUuid(uuid);
        Acte acte = getEmptyActe(currentLocalAuthority);
        acte.setDraft(draft);
        acteRepository.save(acte);
        return new ActeDraftUI(acte.getUuid(), acte.getNumber(), acte.getObjet(), acte.getNature());
    }

    public Acte saveActeDraft(Acte acte, LocalAuthority currentLocalAuthority) {
        acte.setLocalAuthority(currentLocalAuthority);
        acte.setCodeLabel(localAuthorityService.getCodeMatiereLabel(currentLocalAuthority.getUuid(), acte.getCode()));
        if(!currentLocalAuthority.getCanPublishWebSite()) acte.setPublicWebsite(false);
        if(!currentLocalAuthority.getCanPublishRegistre()) acte.setPublic(false);

        updateLastModifiedDraft(acte.getDraft().getUuid());
        return acteRepository.save(acte);
    }

    public void leaveActeDraft(Acte acte, LocalAuthority currentLocalAuthority) {
        // Nothing to save or delete if the acte does not exist anymore
        if(!acteRepository.findByUuidAndDraftNotNull(acte.getUuid()).isPresent()) return;
        if(acte.empty()) acteRepository.delete(acte);
        else saveActeDraft(acte, currentLocalAuthority);
    }

    public Acte saveActeDraftFile(String uuid, MultipartFile file, LocalAuthority currentLocalAuthority) throws IOException {
        Acte acte = StringUtils.isBlank(uuid) ? new Acte() : getActeDraftByUuid(uuid);
        acte.setActeAttachment(new Attachment(file.getBytes(), file.getOriginalFilename(), file.getSize()));
        return saveActeDraft(acte, currentLocalAuthority);
    }

    public Acte saveActeDraftAnnexe(String uuid, MultipartFile file, LocalAuthority currentLocalAuthority) throws IOException {
        Acte acte = StringUtils.isBlank(uuid) ? new Acte() : getActeDraftByUuid(uuid);
        List<Attachment> annexes = acte.getAnnexes();
        annexes.add(new Attachment(file.getBytes(), file.getOriginalFilename(), file.getSize()));
        acte.setAnnexes(annexes);
        return saveActeDraft(acte, currentLocalAuthority);
    }

    public void deleteActeDraftAnnexe(String acteUuid, String uuid) {
        Acte acte = getActeDraftByUuid(acteUuid);
        if(acte.getAnnexes().stream().anyMatch( attachment -> attachment.getUuid().equals(uuid))) {
            List<Attachment> annexes = acte.getAnnexes().stream().filter(attachment -> !attachment.getUuid().equals(uuid)).collect(Collectors.toList());
            acte.setAnnexes(annexes);
            acteRepository.save(acte);
            attachmentRepository.delete(attachmentRepository.findByUuid(uuid).get());
            updateLastModifiedDraft(acte.getDraft().getUuid());
        }
    }

    public void deleteActeDraftFile(String uuid) {
        Acte acte = getActeDraftByUuid(uuid);
        Attachment file = acte.getActeAttachment();
        if(file != null) {
            acte.setActeAttachment(null);
            acteRepository.save(acte);
            attachmentRepository.delete(file);
            updateLastModifiedDraft(acte.getDraft().getUuid());
        }
    }

    private void updateLastModifiedDraft(String uuid) {
        Draft draft = getDraftByUuid(uuid);
        draft.setLastModified(LocalDateTime.now());
        acteDraftRepository.save(draft);
    }

    public List<Acte> getActeDrafts() {
        return acteRepository.findAllByDraftNotNullOrderByDraft_LastModifiedDesc();
    }

    public List<DraftUI> getDraftUIs() {
        List<Draft> drafts = acteDraftRepository.findAllByOrderByLastModifiedDesc();
        List<DraftUI> draftUIs = new ArrayList<>();
        for(Draft draft : drafts) {
            List<ActeDraftUI> acteUuids = getActeDrafts(draft.getUuid()).stream().map(acte ->
                    new ActeDraftUI(acte.getUuid(), acte.getNumber(), acte.getObjet(), acte.getNature())
            ).collect(Collectors.toList());
            if(acteUuids.size() > 0) draftUIs.add(new DraftUI(draft.getUuid(), acteUuids, draft.getLastModified(), draft.getMode(), draft.getDecision(), draft.getNature()));
        }
        return draftUIs;
    }

    public DraftUI getDraftActesUI(String uuid) {
        Draft draft = getDraftByUuid(uuid);
        List<ActeDraftUI> acteUuids = getActeDrafts(uuid).stream().map(acte ->
                new ActeDraftUI(acte.getUuid(), acte.getNumber(), acte.getObjet(), acte.getNature())
        ).collect(Collectors.toList());
        return new DraftUI(uuid, acteUuids, draft.getLastModified(), draft.getMode(), draft.getDecision(), draft.getNature());
    }

    public void deleteDrafts(List<String> uuids) {
        // TODO : filter by current USER
        if(uuids.size() == 0) uuids = acteDraftRepository.findAll().stream().map(Draft::getUuid).collect(Collectors.toList());
        uuids.forEach(uuid -> {
            List<Acte> actes = getActeDrafts(uuid);
            actes.forEach(acteRepository::delete);
            acteDraftRepository.delete(acteDraftRepository.findByUuid(uuid));
        });
    }

    public void deleteActeDraftByUuid(String uuid) {
        acteRepository.delete(getActeDraftByUuid(uuid));
    }

    public Acte getActeDraftByUuid(String uuid) {
        return acteRepository.findByUuidAndDraftNotNull(uuid).orElseThrow(ActeNotFoundException::new);
    }
    public List<Acte> getActeDrafts(String uuid) {
        return acteRepository.findAllByDraftNotNullAndDraft_Uuid(uuid);
    }

    public Draft getDraftByUuid(String uuid) {
        return acteDraftRepository.findByUuid(uuid);
    }
}
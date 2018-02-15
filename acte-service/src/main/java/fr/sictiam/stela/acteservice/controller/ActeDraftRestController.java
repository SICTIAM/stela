package fr.sictiam.stela.acteservice.controller;

import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.model.ActeMode;
import fr.sictiam.stela.acteservice.model.LocalAuthority;
import fr.sictiam.stela.acteservice.model.ui.ActeDraftUI;
import fr.sictiam.stela.acteservice.model.ui.CustomValidationUI;
import fr.sictiam.stela.acteservice.model.ui.DraftUI;
import fr.sictiam.stela.acteservice.service.DraftService;
import fr.sictiam.stela.acteservice.service.LocalAuthorityService;
import fr.sictiam.stela.acteservice.validation.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/acte")
public class ActeDraftRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActeDraftRestController.class);

    private final DraftService draftService;
    private final LocalAuthorityService localAuthorityService;

    @Autowired
    public ActeDraftRestController(DraftService draftService, LocalAuthorityService localAuthorityService) {
        this.draftService = draftService;
        this.localAuthorityService = localAuthorityService;
    }

    @GetMapping("/draft/{mode}")
    public ResponseEntity<Acte> newDraft(
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @PathVariable ActeMode mode) {
        LocalAuthority currentLocalAuthority = localAuthorityService.getByUuid(currentLocalAuthUuid);
        Acte acte = draftService.newDraft(currentLocalAuthority, mode);
        return new ResponseEntity<>(acte, HttpStatus.OK);
    }

    @GetMapping("/draft/batch")
    public ResponseEntity<DraftUI> newBatchedDraft(
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid) {
        LocalAuthority currentLocalAuthority = localAuthorityService.getByUuid(currentLocalAuthUuid);
        DraftUI draft = draftService.newBatchedDraft(currentLocalAuthority);
        return new ResponseEntity<>(draft, HttpStatus.OK);
    }

    @GetMapping("/drafts")
    public ResponseEntity<List<DraftUI>> getDrafts() {
        List<DraftUI> drafts = draftService.getDraftUIs();
        return new ResponseEntity<>(drafts, HttpStatus.OK);
    }

    @DeleteMapping("/drafts")
    public ResponseEntity<?> deleteDrafts(@RequestBody List<String> uuids) {
        draftService.deleteDrafts(uuids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/drafts/{uuid}")
    public ResponseEntity<DraftUI> getDraftByUuid(@PathVariable String uuid) {
        DraftUI draft = draftService.getDraftActesUI(uuid);
        return new ResponseEntity<>(draft, HttpStatus.OK);
    }

    @GetMapping("/drafts/{draftUuid}/{uuid}")
    public ResponseEntity<Acte> getActeDraftByUuid(@PathVariable String uuid) {
        Acte acte = draftService.getActeDraftByUuid(uuid);
        return new ResponseEntity<>(acte, HttpStatus.OK);
    }

    @PutMapping("/drafts/{draftUuid}/{uuid}")
    ResponseEntity<String> saveActeDraft(
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestBody Acte acte) {
        // TODO Retrieve current local authority
        LocalAuthority currentLocalAuthority = localAuthorityService.getByUuid(currentLocalAuthUuid);
        Acte result = draftService.saveActeDraft(acte, currentLocalAuthority);
        return new ResponseEntity<>(result.getUuid(), HttpStatus.OK);
    }

    @PostMapping("/drafts/{draftUuid}")
    ResponseEntity<?> submitDraft(@PathVariable String draftUuid,
            @RequestAttribute("STELA-Current-Profile-UUID") String profileUuid) {
        Optional opt = draftService.sumitDraft(draftUuid, profileUuid);
        if (opt.isPresent())
            return new ResponseEntity<>(opt.get(), HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PatchMapping("/drafts/{draftUuid}")
    ResponseEntity<?> upddateDraftFields(@RequestBody DraftUI draftUI) {
        draftService.upddateDraftFields(draftUI);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping("/drafts/{draftUuid}")
    ResponseEntity<?> deleteDraft(@PathVariable String draftUuid) {
        draftService.deleteDrafts(Collections.singletonList(draftUuid));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/drafts/{draftUuid}/{uuid}")
    ResponseEntity<?> submitActeDraft(@PathVariable String uuid,
            @RequestAttribute("STELA-Current-Profile-UUID") String profileUuid) {
        Acte acteDraft = draftService.getActeDraftByUuid(uuid);
        List<ObjectError> errors = ValidationUtil.validateActe(acteDraft);
        if (!errors.isEmpty()) {
            CustomValidationUI customValidationUI = new CustomValidationUI(errors, "has failed");
            return new ResponseEntity<>(customValidationUI, HttpStatus.BAD_REQUEST);
        } else {
            acteDraft.setProfileUuid(profileUuid);
            Acte result = draftService.submitActeDraft(acteDraft);
            return new ResponseEntity<>(result.getUuid(), HttpStatus.CREATED);
        }
    }

    @PostMapping("/drafts/{uuid}/newActe")
    public ResponseEntity<ActeDraftUI> newActeForDraft(
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @PathVariable String uuid) {
        // TODO Retrieve current local authority
        LocalAuthority currentLocalAuthority = localAuthorityService.getByUuid(currentLocalAuthUuid);
        ActeDraftUI acte = draftService.newActeForDraft(uuid, currentLocalAuthority);
        return new ResponseEntity<>(acte, HttpStatus.OK);
    }

    @DeleteMapping("/drafts/{uuid}/{acteUuid}")
    public ResponseEntity<?> deleteActeDraft(@PathVariable("acteUuid") String acteUuid) {
        draftService.deleteActeDraftByUuid(acteUuid);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/drafts/{draftUuid}/{uuid}/leave")
    public ResponseEntity<?> leaveActeDraft(
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestBody Acte acte) {
        LocalAuthority currentLocalAuthority = localAuthorityService.getByUuid(currentLocalAuthUuid);
        draftService.leaveActeDraft(acte, currentLocalAuthority);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/drafts/{draftUuid}/{uuid}/file")
    public ResponseEntity<Acte> saveActeDraftFile(
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @PathVariable String uuid, @RequestParam("file") MultipartFile file) {
        LocalAuthority currentLocalAuthority = localAuthorityService.getByUuid(currentLocalAuthUuid);
        try {
            Acte acte = draftService.saveActeDraftFile(uuid, file, currentLocalAuthority);
            return new ResponseEntity<>(acte, HttpStatus.OK);
        } catch (IOException e) {
            LOGGER.error("Error while trying to save file: {}", e);
            return new ResponseEntity<>(draftService.getActeDraftByUuid(uuid), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/drafts/{draftUuid}/{uuid}/annexe")
    public ResponseEntity<Acte> saveActeDraftAnnexe(
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @PathVariable String uuid, @RequestParam("file") MultipartFile file) {
        LocalAuthority currentLocalAuthority = localAuthorityService.getByUuid(currentLocalAuthUuid);
        try {
            Acte acte = draftService.saveActeDraftAnnexe(uuid, file, currentLocalAuthority);
            return new ResponseEntity<>(acte, HttpStatus.OK);
        } catch (IOException e) {
            LOGGER.error("Error while trying to save annexe: {}", e);
            return new ResponseEntity<>(draftService.getActeDraftByUuid(uuid), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/drafts/{draftUuid}/{acteUuid}/file/type/{uuid}")
    public ResponseEntity updateFileAttachmentType(@PathVariable String acteUuid, @PathVariable String uuid) {
        draftService.updateActeFileAttachmentType(acteUuid, uuid);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/drafts/{draftUuid}/{acteUuid}/annexe/{annexeUuid}/type/{uuid}")
    public ResponseEntity updateAnnexeAttachmentType(@PathVariable String annexeUuid, @PathVariable String uuid) {
        draftService.updateAttachmentType(annexeUuid, uuid);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/drafts/{draftUuid}/types")
    public ResponseEntity removeAttachmentTypes(@PathVariable String draftUuid) {
        draftService.removeAttachmentTypes(draftUuid);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/drafts/{draftUuid}/{uuid}/file")
    public ResponseEntity<?> deleteActeDraftFile(@PathVariable String uuid) {
        draftService.deleteActeDraftFile(uuid);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/drafts/{draftUuid}/{acteUuid}/annexe/{uuid}")
    public ResponseEntity<?> deleteActeDraftAnnexe(@PathVariable String acteUuid, @PathVariable String uuid) {
        draftService.deleteActeDraftAnnexe(acteUuid, uuid);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}

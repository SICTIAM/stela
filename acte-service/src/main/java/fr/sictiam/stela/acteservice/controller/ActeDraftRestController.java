package fr.sictiam.stela.acteservice.controller;

import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.model.ActeMode;
import fr.sictiam.stela.acteservice.model.ActeNature;
import fr.sictiam.stela.acteservice.model.LocalAuthority;
import fr.sictiam.stela.acteservice.model.Right;
import fr.sictiam.stela.acteservice.model.ui.ActeDraftUI;
import fr.sictiam.stela.acteservice.model.ui.CustomValidationUI;
import fr.sictiam.stela.acteservice.model.ui.DraftUI;
import fr.sictiam.stela.acteservice.model.util.Certificate;
import fr.sictiam.stela.acteservice.service.DraftService;
import fr.sictiam.stela.acteservice.service.LocalAuthorityService;
import fr.sictiam.stela.acteservice.service.util.CertUtilService;
import fr.sictiam.stela.acteservice.service.util.RightUtils;
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
import java.util.Set;

@RestController
@RequestMapping("/api/acte")
public class ActeDraftRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActeDraftRestController.class);

    private final DraftService draftService;
    private final LocalAuthorityService localAuthorityService;
    private final CertUtilService certUtilService;

    @Autowired
    public ActeDraftRestController(DraftService draftService, LocalAuthorityService localAuthorityService,
            CertUtilService certUtilService) {
        this.draftService = draftService;
        this.localAuthorityService = localAuthorityService;
        this.certUtilService = certUtilService;
    }

    @GetMapping("/draft/{mode}")
    public ResponseEntity<Acte> newDraft(@RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @PathVariable ActeMode mode) {
        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.ACTES_DEPOSIT))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        LocalAuthority currentLocalAuthority = localAuthorityService.getByUuid(currentLocalAuthUuid);
        Acte acte = draftService.newDraft(currentLocalAuthority, mode);
        return new ResponseEntity<>(acte, HttpStatus.OK);
    }

    @GetMapping("/draft/batch")
    public ResponseEntity<DraftUI> newBatchedDraft(@RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid) {
        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.ACTES_DEPOSIT))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        LocalAuthority currentLocalAuthority = localAuthorityService.getByUuid(currentLocalAuthUuid);
        DraftUI draft = draftService.newBatchedDraft(currentLocalAuthority);
        return new ResponseEntity<>(draft, HttpStatus.OK);
    }

    @GetMapping("/drafts")
    public ResponseEntity<List<DraftUI>> getDrafts(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights) {
        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.ACTES_DEPOSIT))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        List<DraftUI> drafts = draftService.getDraftUIs();
        return new ResponseEntity<>(drafts, HttpStatus.OK);
    }

    @DeleteMapping("/drafts")
    public ResponseEntity<?> deleteDrafts(@RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestBody List<String> uuids) {
        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.ACTES_DEPOSIT))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        draftService.deleteDrafts(uuids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/drafts/{uuid}")
    public ResponseEntity<DraftUI> getDraftByUuid(@RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @PathVariable String uuid) {
        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.ACTES_DEPOSIT))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        DraftUI draft = draftService.getDraftActesUI(uuid);
        return new ResponseEntity<>(draft, HttpStatus.OK);
    }

    @GetMapping("/drafts/{draftUuid}/{uuid}")
    public ResponseEntity<Acte> getActeDraftByUuid(@RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @PathVariable String uuid) {
        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.ACTES_DEPOSIT))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Acte acte = draftService.getActeDraftByUuid(uuid);
        return new ResponseEntity<>(acte, HttpStatus.OK);
    }

    @PutMapping("/drafts/{draftUuid}/{uuid}")
    ResponseEntity<String> saveActeDraft(@RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestBody Acte acte) {
        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.ACTES_DEPOSIT))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        LocalAuthority currentLocalAuthority = localAuthorityService.getByUuid(currentLocalAuthUuid);
        Acte result = draftService.saveOrUpdateActeDraft(acte, currentLocalAuthority);
        return new ResponseEntity<>(result.getUuid(), HttpStatus.OK);
    }

    @PostMapping("/drafts/{draftUuid}")
    ResponseEntity<?> submitDraft(@PathVariable String draftUuid,
            @RequestAttribute("STELA-Certificate") Certificate certificate,
            @RequestAttribute("STELA-Current-Profile-Paired-Certificate") Certificate pairedCertificate,
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Profile-UUID") String profileUuid) {
        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.ACTES_DEPOSIT))
                || !certUtilService.checkCert(certificate, pairedCertificate)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional opt = draftService.sumitDraft(draftUuid, profileUuid);
        if (opt.isPresent())
            return new ResponseEntity<>(opt.get(), HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PatchMapping("/drafts/{draftUuid}")
    ResponseEntity<?> upddateDraftFields(@RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestBody DraftUI draftUI) {
        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.ACTES_DEPOSIT))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        draftService.upddateDraftFields(draftUI);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping("/drafts/{draftUuid}")
    ResponseEntity<?> deleteDraft(@RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @PathVariable String draftUuid) {
        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.ACTES_DEPOSIT))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        draftService.deleteDrafts(Collections.singletonList(draftUuid));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/drafts/{draftUuid}/{uuid}")
    ResponseEntity<?> submitActeDraft(@PathVariable String uuid,
            @RequestAttribute("STELA-Certificate") Certificate certificate,
            @RequestAttribute("STELA-Current-Profile-Paired-Certificate") Certificate pairedCertificate,
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Profile-UUID") String profileUuid) {
        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.ACTES_DEPOSIT))
                || !certUtilService.checkCert(certificate, pairedCertificate)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
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
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @PathVariable String uuid) {
        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.ACTES_DEPOSIT))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        LocalAuthority currentLocalAuthority = localAuthorityService.getByUuid(currentLocalAuthUuid);
        ActeDraftUI acte = draftService.newActeForDraft(uuid, currentLocalAuthority);
        return new ResponseEntity<>(acte, HttpStatus.OK);
    }

    @DeleteMapping("/drafts/{uuid}/{acteUuid}")
    public ResponseEntity<?> deleteActeDraft(@RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @PathVariable("acteUuid") String acteUuid) {
        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.ACTES_DEPOSIT))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        draftService.deleteActeDraftByUuid(acteUuid);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/drafts/{draftUuid}/{uuid}/leave")
    public ResponseEntity<?> leaveActeDraft(@RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestBody Acte acte) {
        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.ACTES_DEPOSIT))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        LocalAuthority currentLocalAuthority = localAuthorityService.getByUuid(currentLocalAuthUuid);
        draftService.leaveActeDraft(acte, currentLocalAuthority);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/drafts/{draftUuid}/{uuid}/file")
    public ResponseEntity<?> saveActeDraftFile(@RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @PathVariable String uuid, @RequestParam("file") MultipartFile file,
            @RequestParam(value = "nature", required = false) ActeNature nature) {
        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.ACTES_DEPOSIT))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        List<ObjectError> objectErrors = ValidationUtil.validateFile(file, nature, true, "acteAttachment");
        if (!objectErrors.isEmpty()) {
            return new ResponseEntity<>(new CustomValidationUI(objectErrors, "has failed"), HttpStatus.BAD_REQUEST);
        }
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
    public ResponseEntity<?> saveActeDraftAnnexe(@RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @PathVariable String uuid, @RequestParam("file") MultipartFile file,
            @RequestParam(value = "nature", required = false) ActeNature nature) {
        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.ACTES_DEPOSIT))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        List<ObjectError> objectErrors = ValidationUtil.validateFile(file, nature, false, "acte.extensions");
        if (!objectErrors.isEmpty()) {
            return new ResponseEntity<>(new CustomValidationUI(objectErrors, "has failed"), HttpStatus.BAD_REQUEST);
        }
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
    public ResponseEntity updateFileAttachmentType(@RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @PathVariable String acteUuid, @PathVariable String uuid) {
        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.ACTES_DEPOSIT))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        draftService.updateActeFileAttachmentType(acteUuid, uuid);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/drafts/{draftUuid}/{acteUuid}/annexe/{annexeUuid}/type/{uuid}")
    public ResponseEntity updateAnnexeAttachmentType(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights, @PathVariable String annexeUuid,
            @PathVariable String uuid) {
        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.ACTES_DEPOSIT))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        draftService.updateAttachmentType(annexeUuid, uuid);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/drafts/{draftUuid}/types")
    public ResponseEntity removeAttachmentTypes(@RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @PathVariable String draftUuid) {
        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.ACTES_DEPOSIT))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        draftService.removeAttachmentTypes(draftUuid);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/drafts/{draftUuid}/{uuid}/file")
    public ResponseEntity<?> deleteActeDraftFile(@RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @PathVariable String uuid) {
        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.ACTES_DEPOSIT))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        draftService.deleteActeDraftFile(uuid);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/drafts/{draftUuid}/{acteUuid}/annexe/{uuid}")
    public ResponseEntity<?> deleteActeDraftAnnexe(@RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @PathVariable String acteUuid, @PathVariable String uuid) {
        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.ACTES_DEPOSIT))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        draftService.deleteActeDraftAnnexe(acteUuid, uuid);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}

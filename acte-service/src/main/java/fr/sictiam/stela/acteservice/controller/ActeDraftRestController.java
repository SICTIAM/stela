package fr.sictiam.stela.acteservice.controller;

import fr.sictiam.stela.acteservice.model.*;
import fr.sictiam.stela.acteservice.model.ui.ActeDraftUI;
import fr.sictiam.stela.acteservice.model.ui.DraftUI;
import fr.sictiam.stela.acteservice.service.DraftService;
import fr.sictiam.stela.acteservice.service.LocalAuthorityService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/acte")
public class ActeDraftRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActeDraftRestController.class);

    private final DraftService draftService;
    private final LocalAuthorityService localAuthorityService;

    @Autowired
    public ActeDraftRestController(DraftService draftService, LocalAuthorityService localAuthorityService){
        this.draftService = draftService;
        this.localAuthorityService = localAuthorityService;
    }

    @GetMapping("/draft/{mode}")
    public ResponseEntity<Acte> newDraft(@PathVariable ActeMode mode) {
        // TODO Retrieve current local authority
        LocalAuthority currentLocalAuthority = localAuthorityService.getByName("SICTIAM-Test").get();
        Acte acte = draftService.newDraft(currentLocalAuthority, mode);
        return new ResponseEntity<>(acte, HttpStatus.OK);
    }

    @GetMapping("/draft/batch")
    public ResponseEntity<DraftUI> newBatchedDraft() {
        // TODO Retrieve current local authority
        LocalAuthority currentLocalAuthority = localAuthorityService.getByName("SICTIAM-Test").get();
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
    ResponseEntity<String> saveActeDraft(@RequestBody Acte acte) {
        // TODO Retrieve current local authority
        LocalAuthority currentLocalAuthority = localAuthorityService.getByName("SICTIAM-Test").get();
        Acte result = draftService.saveActeDraft(acte, currentLocalAuthority);
        return new ResponseEntity<>(result.getUuid(), HttpStatus.OK);
    }

    @PostMapping("/drafts/{draftUuid}")
    ResponseEntity<?> submitDraft(@PathVariable String draftUuid) {
        draftService.sumitDraft(draftUuid);
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
    ResponseEntity<String> submitActeDraft(@PathVariable String uuid) {
        Acte result = draftService.submitActeDraft(uuid);
        return new ResponseEntity<>(result.getUuid(), HttpStatus.CREATED);
    }

    @PostMapping("/drafts/{uuid}/newActe")
    public ResponseEntity<ActeDraftUI> newActeForDraft(@PathVariable String uuid) {
        // TODO Retrieve current local authority
        LocalAuthority currentLocalAuthority = localAuthorityService.getByName("SICTIAM-Test").get();
        ActeDraftUI acte = draftService.newActeForDraft(uuid, currentLocalAuthority);
        return new ResponseEntity<>(acte, HttpStatus.OK);
    }

    @DeleteMapping("/drafts/{uuid}/{acteUuid}")
    public ResponseEntity<?> deleteActeDraft(@PathVariable("acteUuid") String acteUuid) {
        draftService.deleteActeDraftByUuid(acteUuid);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/drafts/{draftUuid}/{uuid}/leave")
    public ResponseEntity<?> leaveActeDraft(@RequestBody Acte acte) {
        // TODO Retrieve current local authority
        LocalAuthority currentLocalAuthority = localAuthorityService.getByName("SICTIAM-Test").get();
        draftService.leaveActeDraft(acte, currentLocalAuthority);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/drafts/{draftUuid}/{uuid}/file")
    public ResponseEntity<Acte> saveActeDraftFile(@PathVariable String uuid, @RequestParam("file") MultipartFile file) {
        // TODO Retrieve current local authority
        LocalAuthority currentLocalAuthority = localAuthorityService.getByName("SICTIAM-Test").get();
        try {
            Acte acte = draftService.saveActeDraftFile(uuid, file, currentLocalAuthority);
            return new ResponseEntity<>(acte, HttpStatus.OK);
        } catch (IOException e) {
            LOGGER.error("Error while trying to save file: {}", e);
            return new ResponseEntity<>(draftService.getActeDraftByUuid(uuid), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/drafts/{draftUuid}/{uuid}/annexe")
    public ResponseEntity<Acte> saveActeDraftAnnexe(@PathVariable String uuid, @RequestParam("file") MultipartFile file) {
        // TODO Retrieve current local authority
        LocalAuthority currentLocalAuthority = localAuthorityService.getByName("SICTIAM-Test").get();
        try {
            Acte acte = draftService.saveActeDraftAnnexe(uuid, file, currentLocalAuthority);
            return new ResponseEntity<>(acte, HttpStatus.OK);
        } catch (IOException e) {
            LOGGER.error("Error while trying to save annexe: {}", e);
            return new ResponseEntity<>(draftService.getActeDraftByUuid(uuid), HttpStatus.INTERNAL_SERVER_ERROR);
        }
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

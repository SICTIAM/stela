package fr.sictiam.stela.acteservice.controller;

import fr.sictiam.stela.acteservice.model.*;
import fr.sictiam.stela.acteservice.model.ui.CustomValidationUI;
import fr.sictiam.stela.acteservice.service.ActeService;
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
import java.util.List;

@RestController
@RequestMapping("/api/acte")
public class ActeDraftRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActeDraftRestController.class);

    private final ActeService acteService;
    private final LocalAuthorityService localAuthorityService;

    @Autowired
    public ActeDraftRestController(ActeService acteService, LocalAuthorityService localAuthorityService){
        this.acteService = acteService;
        this.localAuthorityService = localAuthorityService;
    }
    @GetMapping("/draft")
    public ResponseEntity<Acte> newDraft() {
        // TODO Retrieve current local authority
        LocalAuthority currentLocalAuthority = localAuthorityService.getByName("SICTIAM-Test").get();
        Acte acte = acteService.saveDraft(new Acte(), currentLocalAuthority);
        return new ResponseEntity<>(acte, HttpStatus.OK);
    }

    @GetMapping("/drafts")
    public ResponseEntity<List<Acte>> getDrafts() {
        List<Acte> actes = acteService.getDrafts();
        return new ResponseEntity<>(actes, HttpStatus.OK);
    }

    @DeleteMapping("/drafts")
    public ResponseEntity<?> deleteDrafts(@RequestBody List<String> uuids) {
        acteService.deleteDrafts(uuids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/drafts/{uuid}")
    public ResponseEntity<Acte> getDraftByUuid(@PathVariable String uuid) {
        Acte acte = acteService.getDraftByUuid(uuid);
        return new ResponseEntity<>(acte, HttpStatus.OK);
    }

    @PutMapping("/drafts/{uuid}")
    ResponseEntity<String> saveDraft(@RequestBody Acte acte) {
        // TODO Retrieve current local authority
        LocalAuthority currentLocalAuthority = localAuthorityService.getByName("SICTIAM-Test").get();
        Acte result = acteService.saveDraft(acte, currentLocalAuthority);
        return new ResponseEntity<>(result.getUuid(), HttpStatus.OK);
    }

    @PostMapping("/drafts/{uuid}")
    ResponseEntity<Object> submitDraft(@PathVariable String uuid) {

	Acte acte = acteService.getDraftByUuid(uuid);
	List<ObjectError> errors = ValidationUtil.validateActe(acte);

	if (!errors.isEmpty()) {
	    CustomValidationUI customValidationUI = new CustomValidationUI(errors, "has failed");
	    return new ResponseEntity<>(customValidationUI, HttpStatus.BAD_REQUEST);
	} else {
	    Acte result = acteService.sendDraft(acte);
	    return new ResponseEntity<>(result.getUuid(), HttpStatus.CREATED);
	}
    }

    @PutMapping("/drafts/{uuid}/leave")
    public ResponseEntity<?> leaveDraft(@RequestBody Acte acte) {
        // TODO Retrieve current local authority
        LocalAuthority currentLocalAuthority = localAuthorityService.getByName("SICTIAM-Test").get();
        acteService.closeDraft(acte, currentLocalAuthority);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/drafts/{uuid}/file")
    public ResponseEntity<Acte> saveDraftFile(@PathVariable String uuid, @RequestParam("file") MultipartFile file) {
        // TODO Retrieve current local authority
        LocalAuthority currentLocalAuthority = localAuthorityService.getByName("SICTIAM-Test").get();
        try {
            Acte acte = acteService.saveDraftFile(uuid, file, currentLocalAuthority);
            return new ResponseEntity<>(acte, HttpStatus.OK);
        } catch (IOException e) {
            LOGGER.error("Error while trying to save file: {}", e);
            return new ResponseEntity<>(acteService.getDraftByUuid(uuid), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/drafts/{uuid}/annexe")
    public ResponseEntity<Acte> saveDraftAnnexe(@PathVariable String uuid, @RequestParam("file") MultipartFile file) {
        // TODO Retrieve current local authority
        LocalAuthority currentLocalAuthority = localAuthorityService.getByName("SICTIAM-Test").get();
        try {
            Acte acte = acteService.saveDraftAnnexe(uuid, file, currentLocalAuthority);
            return new ResponseEntity<>(acte, HttpStatus.OK);
        } catch (IOException e) {
            LOGGER.error("Error while trying to save annexe: {}", e);
            return new ResponseEntity<>(acteService.getDraftByUuid(uuid), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/drafts/{uuid}/file")
    public ResponseEntity<?> deleteDraftFile(@PathVariable String uuid) {
        acteService.deleteDraftFile(uuid);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/drafts/{acteUuid}/annexe/{uuid}")
    public ResponseEntity<?> deleteDraftAnnexe(@PathVariable String acteUuid, @PathVariable String uuid) {
        acteService.deleteDraftAnnexe(acteUuid, uuid);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}

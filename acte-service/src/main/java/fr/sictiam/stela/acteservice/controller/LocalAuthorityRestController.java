package fr.sictiam.stela.acteservice.controller;

import fr.sictiam.stela.acteservice.model.LocalAuthority;
import fr.sictiam.stela.acteservice.model.MaterialCode;
import fr.sictiam.stela.acteservice.model.Right;
import fr.sictiam.stela.acteservice.model.ui.ActeDepositFieldsUI;
import fr.sictiam.stela.acteservice.model.ui.LocalAuthorityUpdateUI;
import fr.sictiam.stela.acteservice.service.LocalAuthorityService;
import fr.sictiam.stela.acteservice.service.util.RightUtils;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/acte/localAuthority")
public class LocalAuthorityRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalAuthorityRestController.class);

    private final LocalAuthorityService localAuthorityService;

    @Autowired
    public LocalAuthorityRestController(LocalAuthorityService localAuthorityService) {
        this.localAuthorityService = localAuthorityService;
    }

    @GetMapping
    public ResponseEntity<List<LocalAuthority>> getAll(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin) {
        if (!isLocalAuthorityAdmin) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        List<LocalAuthority> localAuthorities = localAuthorityService.getAll();
        return new ResponseEntity<>(localAuthorities, HttpStatus.OK);
    }

    @GetMapping("/current")
    public ResponseEntity<LocalAuthority> getCurrent(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid) {
        if (!isLocalAuthorityAdmin) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        LocalAuthority currentLocalAuthority = localAuthorityService.getByUuid(currentLocalAuthUuid);
        return new ResponseEntity<>(currentLocalAuthority, HttpStatus.OK);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<LocalAuthority> getByUuid(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @PathVariable String uuid) {
        if (!isLocalAuthorityAdmin) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        LocalAuthority localAuthority = localAuthorityService.getByUuid(uuid);
        return new ResponseEntity<>(localAuthority, HttpStatus.OK);
    }

    @PatchMapping("/{uuid}")
    public ResponseEntity<LocalAuthority> update(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @PathVariable String uuid, @Valid @RequestBody LocalAuthorityUpdateUI localAuthorityUpdateUI) {
        if (!isLocalAuthorityAdmin) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        LocalAuthority localAuthority = localAuthorityService.getByUuid(uuid);
        try {
            BeanUtils.copyProperties(localAuthority, localAuthorityUpdateUI);
        } catch (Exception e) {
            LOGGER.error("Error while updating properties: {}", e);
            return new ResponseEntity<>(localAuthority, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        localAuthority = localAuthorityService.createOrUpdate(localAuthority);
        return new ResponseEntity<>(localAuthority, HttpStatus.OK);
    }

    @GetMapping("/depositFields")
    public ResponseEntity<ActeDepositFieldsUI> getActeDepositFields(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid) {
        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.ACTES_DEPOSIT))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        LocalAuthority currentLocalAuthority = localAuthorityService.getByUuid(currentLocalAuthUuid);

        LOGGER.info("currentLocalAuthority: {}", currentLocalAuthority.getName());
        return new ResponseEntity<>(new ActeDepositFieldsUI(currentLocalAuthority.getCanPublishRegistre(),
                currentLocalAuthority.getCanPublishWebSite()), HttpStatus.OK);
    }

    @GetMapping("/codes-matieres")
    public ResponseEntity<List<MaterialCode>> getCodesMatieres(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid) {
        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.ACTES_DEPOSIT))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        LocalAuthority currentLocalAuthority = localAuthorityService.getByUuid(currentLocalAuthUuid);
        return new ResponseEntity<>(localAuthorityService.getCodesMatieres(currentLocalAuthority.getUuid()),
                HttpStatus.OK);
    }

    @GetMapping("/codes-matiere/{code}")
    public ResponseEntity<String> getCodeMatiereLabel(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @PathVariable String code) {
        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.ACTES_DEPOSIT))) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        LocalAuthority currentLocalAuthority = localAuthorityService.getByUuid(currentLocalAuthUuid);
        return new ResponseEntity<>(localAuthorityService.getCodeMatiereLabel(currentLocalAuthority.getUuid(), code),
                HttpStatus.OK);
    }
}

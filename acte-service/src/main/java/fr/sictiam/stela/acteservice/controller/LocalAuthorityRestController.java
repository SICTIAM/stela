package fr.sictiam.stela.acteservice.controller;

import java.util.List;

import javax.validation.Valid;

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

import fr.sictiam.stela.acteservice.model.LocalAuthority;
import fr.sictiam.stela.acteservice.model.MaterialCode;
import fr.sictiam.stela.acteservice.model.ui.ActeDepositFieldsUI;
import fr.sictiam.stela.acteservice.model.ui.LocalAuthorityUpdateUI;
import fr.sictiam.stela.acteservice.service.LocalAuthorityService;

@RestController
@RequestMapping("/api/acte/localAuthority")
public class LocalAuthorityRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalAuthorityRestController.class);

    private final LocalAuthorityService localAuthorityService;

    @Autowired
    public LocalAuthorityRestController(LocalAuthorityService localAuthorityService){
        this.localAuthorityService = localAuthorityService;
    }

    @GetMapping
    public ResponseEntity<List<LocalAuthority>> getAll() {
        List<LocalAuthority> localAuthorities = localAuthorityService.getAll();
        return new ResponseEntity<>(localAuthorities, HttpStatus.OK);
    }

    @GetMapping("/current")
    public ResponseEntity<LocalAuthority> getCurrent(@RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid) {
        LocalAuthority currentLocalAuthority = localAuthorityService.getByUuid(currentLocalAuthUuid);
        return new ResponseEntity<>(currentLocalAuthority, HttpStatus.OK);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<LocalAuthority> getByUuid(@PathVariable String uuid) {
        LocalAuthority localAuthority = localAuthorityService.getByUuid(uuid);
        return new ResponseEntity<>(localAuthority, HttpStatus.OK);
    }

    @PatchMapping("/{uuid}")
    public ResponseEntity<LocalAuthority> update(@PathVariable String uuid, @Valid @RequestBody LocalAuthorityUpdateUI localAuthorityUpdateUI) {
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
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid) {
        LocalAuthority currentLocalAuthority = localAuthorityService.getByUuid(currentLocalAuthUuid);
        
        LOGGER.info("currentLocalAuthority: {}", currentLocalAuthority.getName());
        return new ResponseEntity<>(new ActeDepositFieldsUI(currentLocalAuthority.getCanPublishRegistre(),
                currentLocalAuthority.getCanPublishWebSite()), HttpStatus.OK);
    }
    
    @GetMapping("/load-matieres")
    public void loadCodesMatieres(@RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid) {
                LocalAuthority currentLocalAuthority = localAuthorityService.getByUuid(currentLocalAuthUuid);
        localAuthorityService.loadCodesMatieres(currentLocalAuthority.getUuid());
    }
    
    @GetMapping("/codes-matieres")
    public ResponseEntity<List<MaterialCode>> getCodesMatieres(@RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid) {
                LocalAuthority currentLocalAuthority = localAuthorityService.getByUuid(currentLocalAuthUuid);
        return new ResponseEntity<>(localAuthorityService.getCodesMatieres(currentLocalAuthority.getUuid()), HttpStatus.OK);
    }
    
    @GetMapping("/codes-matiere/{code}")
    public ResponseEntity< String> getCodeMatiereLabel(@RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid, @PathVariable String code) {
        LocalAuthority currentLocalAuthority = localAuthorityService.getByUuid(currentLocalAuthUuid);
        return new ResponseEntity<>(localAuthorityService.getCodeMatiereLabel(currentLocalAuthority.getUuid(), code), HttpStatus.OK);
    }
}

package fr.sictiam.stela.acteservice.controller;

import fr.sictiam.stela.acteservice.model.LocalAuthority;
import fr.sictiam.stela.acteservice.model.MaterialCode;
import fr.sictiam.stela.acteservice.model.ui.ActeDepositFieldsUI;
import fr.sictiam.stela.acteservice.model.ui.LocalAuthorityUpdateUI;
import fr.sictiam.stela.acteservice.service.LocalAuthorityService;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<ActeDepositFieldsUI> getActeDepositFields() {
        // TODO: Retrieve current LocalAuthority
        LocalAuthority currentLocalAuthority = localAuthorityService.getByName("SICTIAM-Test").get();
        LOGGER.info("currentLocalAuthority: {}", currentLocalAuthority.getName());
        return new ResponseEntity<>(new ActeDepositFieldsUI(currentLocalAuthority.getCanPublishRegistre(),
                currentLocalAuthority.getCanPublishWebSite()), HttpStatus.OK);
    }
    
    @GetMapping("/load-matieres")
    public void loadCodesMatieres() {
        // TODO: Retrieve current LocalAuthority
        LocalAuthority currentLocalAuthority = localAuthorityService.getByName("SICTIAM-Test").get();
        localAuthorityService.loadCodesMatieres(currentLocalAuthority.getUuid());
    }
    
    @GetMapping("/codes-matieres")
    public ResponseEntity<List<MaterialCode>> getCodesMatieres() {
        // TODO: Retrieve current LocalAuthority
        LocalAuthority currentLocalAuthority = localAuthorityService.getByName("SICTIAM-Test").get();
        return new ResponseEntity<>(localAuthorityService.getCodesMatieres(currentLocalAuthority.getUuid()), HttpStatus.OK);
    }
    
    @GetMapping("/codes-matiere/{code}")
    public ResponseEntity< String> getCodeMatiereLabel(@PathVariable String code) {
        // TODO: Retrieve current LocalAuthority
        LocalAuthority currentLocalAuthority = localAuthorityService.getByName("SICTIAM-Test").get();
        return new ResponseEntity<>(localAuthorityService.getCodeMatiereLabel(currentLocalAuthority.getUuid(), code), HttpStatus.OK);
    }
}

package fr.sictiam.stela.pesservice.controller;

import fr.sictiam.stela.pesservice.model.LocalAuthority;
import fr.sictiam.stela.pesservice.model.ServerCode;
import fr.sictiam.stela.pesservice.model.migration.MigrationStatus;
import fr.sictiam.stela.pesservice.model.ui.LocalAuthorityUpdateUI;
import fr.sictiam.stela.pesservice.service.LocalAuthorityService;
import fr.sictiam.stela.pesservice.service.MigrationService;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/pes/localAuthority")
public class LocalAuthorityRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalAuthorityRestController.class);

    private final LocalAuthorityService localAuthorityService;
    private final MigrationService migrationService;

    @Autowired
    public LocalAuthorityRestController(LocalAuthorityService localAuthorityService, MigrationService migrationService) {
        this.localAuthorityService = localAuthorityService;
        this.migrationService = migrationService;
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
        LocalAuthority currentLocalAuthority = localAuthorityService.getByUuid(uuid);
        return new ResponseEntity<>(currentLocalAuthority, HttpStatus.OK);
    }

    @PatchMapping("/{uuid}")
    public ResponseEntity updateParams(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @PathVariable String uuid, @RequestBody @Valid LocalAuthorityUpdateUI localAuthorityUpdateUI) {
        if (!isLocalAuthorityAdmin) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        LocalAuthority localAuthority = localAuthorityService.getByUuid(uuid);
        try {
            BeanUtils.copyProperties(localAuthority, localAuthorityUpdateUI);
        } catch (Exception e) {
            LOGGER.error("Error while updating properties: {}", e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        localAuthorityService.createOrUpdate(localAuthority);
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/server-codes")
    public ResponseEntity<List<ServerCode>> getServerCodes(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin) {
        if (!isLocalAuthorityAdmin) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(Arrays.asList(ServerCode.values()), HttpStatus.OK);
    }

    @PostMapping("/current/migration/{migrationType}")
    public ResponseEntity migrationFromCurrent(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @PathVariable String migrationType,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "siren", required = false) String siren) {
        return migration(migrationType, isLocalAuthorityAdmin, currentLocalAuthUuid, email, siren);
    }

    @PostMapping("/{uuid}/migration/{migrationType}")
    public ResponseEntity migrationByUuid(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @PathVariable String uuid, @PathVariable String migrationType,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "siren", required = false) String siren) {
        return migration(migrationType, isLocalAuthorityAdmin, uuid, email, siren);
    }

    private ResponseEntity migration(String migrationType, boolean isLocalAuthorityAdmin, String localAuthUuid, String email, String siren) {
        if (!isLocalAuthorityAdmin) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        LocalAuthority localAuthority = localAuthorityService.getByUuid(localAuthUuid);
        // TODO : Add the migrationUsersDeactivation
        if ("migrationUsers".equals(migrationType)) {
            if (localAuthority.getMigration() != null && !localAuthority.getMigration().getMigrationUsers().equals(MigrationStatus.NOT_DONE)) {
                return new ResponseEntity(HttpStatus.PRECONDITION_FAILED);
            }
            CompletableFuture.runAsync(() -> migrationService.migrateStela2Users(localAuthority, siren, email));
        } else if ("migrationData".equals(migrationType)) {
            if (localAuthority.getMigration() != null && !localAuthority.getMigration().getMigrationData().equals(MigrationStatus.NOT_DONE)) {
                return new ResponseEntity(HttpStatus.PRECONDITION_FAILED);
            }
            CompletableFuture.runAsync(() -> migrationService.migrateStela2PES(localAuthority, siren, email));
        } else {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/current/migration/{migrationType}/reset")
    public ResponseEntity resetMigrationFromCurrent(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @PathVariable String migrationType) {
        return resetMigration(migrationType, isLocalAuthorityAdmin, currentLocalAuthUuid);
    }

    @PostMapping("/{uuid}/migration/{migrationType}/reset")
    public ResponseEntity resetMigrationByUuid(
            @RequestAttribute("STELA-Current-Profile-Is-Local-Authority-Admin") boolean isLocalAuthorityAdmin,
            @PathVariable String uuid, @PathVariable String migrationType) {
        return resetMigration(migrationType, isLocalAuthorityAdmin, uuid);
    }

    private ResponseEntity resetMigration(String migrationType, boolean isLocalAuthorityAdmin, String localAuthUuid) {
        if (!isLocalAuthorityAdmin) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        migrationService.resetMigration(migrationType, localAuthUuid);
        return new ResponseEntity(HttpStatus.OK);
    }

}

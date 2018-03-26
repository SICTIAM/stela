package fr.sictiam.stela.pesservice.controller;

import fr.sictiam.stela.pesservice.model.LocalAuthority;
import fr.sictiam.stela.pesservice.service.LocalAuthorityService;
import fr.sictiam.stela.pesservice.service.MigrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/pes/migration")
public class MigrationController {

    private final MigrationService migrationService;
    private final LocalAuthorityService localAuthorityService;

    public MigrationController(MigrationService migrationService, LocalAuthorityService localAuthorityService) {
        this.migrationService = migrationService;
        this.localAuthorityService = localAuthorityService;
    }

    @GetMapping
    public ResponseEntity<?> migrateStela2() {
        String siren = "284767423";
        Optional<LocalAuthority> localAuthorityOpt = localAuthorityService.getBySirenOrSirens(siren);
        if (localAuthorityOpt.isPresent()) migrationService.migrateStela2PES(localAuthorityOpt.get(), siren);
        else {
            return new ResponseEntity<>("notifications.admin.local_authority_not_found", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(HttpStatus.OK);
    }
}

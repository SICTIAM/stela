package fr.sictiam.stela.pesservice.controller;

import fr.sictiam.stela.pesservice.model.LocalAuthority;
import fr.sictiam.stela.pesservice.model.PesAller;
import fr.sictiam.stela.pesservice.model.SesileConfiguration;
import fr.sictiam.stela.pesservice.model.sesile.ServiceOrganisation;
import fr.sictiam.stela.pesservice.service.LocalAuthorityService;
import fr.sictiam.stela.pesservice.service.PesAllerService;
import fr.sictiam.stela.pesservice.service.SesileService;
import fr.sictiam.stela.pesservice.service.exceptions.PesNotFoundException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/pes/sesile")
public class SesileController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SesileController.class);

    private final SesileService sesileService;
    private final LocalAuthorityService localAuthorityService;
    private final PesAllerService pesAllerService;

    public SesileController(SesileService sesileService, LocalAuthorityService localAuthorityService,
            PesAllerService pesAllerService) {
        this.sesileService = sesileService;
        this.localAuthorityService = localAuthorityService;
        this.pesAllerService = pesAllerService;
    }

    @GetMapping("/organisations")
    public List<ServiceOrganisation> getCurrentOrganisations(
            @RequestAttribute("STELA-Current-Profile-UUID") String profileUuid,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid) {
        LocalAuthority localAuthority = localAuthorityService.getByUuid(currentLocalAuthUuid);
        try {
            return sesileService.getServiceOrganisations(localAuthority, profileUuid);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return Collections.emptyList();
    }

    @GetMapping("/organisations/{localAuthUuid}/{profileUuid}")
    public List<ServiceOrganisation> getCurrentOrganisationsByProfileUuid(@PathVariable String localAuthUuid,
            @PathVariable String profileUuid) {
        LocalAuthority localAuthority = localAuthorityService.getByUuid(localAuthUuid);
        try {
            return sesileService.getServiceOrganisations(localAuthority, profileUuid);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return Collections.emptyList();
    }

    @GetMapping("/subscription/{localAuthUuid}")
    public Boolean getCurrentOrganisationsByProfileUuid(@PathVariable String localAuthUuid) {
        LocalAuthority localAuthority = localAuthorityService.getByUuid(localAuthUuid);
        return localAuthority.getSesileSubscription();
    }

    @PostMapping("/configuration")
    public SesileConfiguration createOrUpdateConfiguration(@RequestBody SesileConfiguration sesileConfiguration) {
        return sesileService.createOrUpdate(sesileConfiguration);
    }

    @GetMapping("/configuration/{uuid}")
    public SesileConfiguration getConfigurationByUuid(@PathVariable String uuid) {
        return sesileService.getConfigurationByUuid(uuid);
    }

    @PostMapping("/verify-tokens")
    public boolean verifyTokens(@RequestParam String token, @RequestParam String secret,
            @RequestParam boolean sesileNewVersion) {
        return sesileService.verifyTokens(token, secret, sesileNewVersion);
    }

    /**
     * @return ResponseEntity with status codes :
     * 200: Ok, PES updated
     * 400: Invalid status value
     * 401: Unauthorized : token missing or invalid token
     * 404: PES not found from uuid
     * 500: Stela internal error
     */
    @PostMapping("/signature-hook/{token}/{uuid}/{status}")
    public ResponseEntity<String> updateStatus(@PathVariable String token, @PathVariable String uuid,
            @PathVariable String status,
            @RequestParam(required = false) MultipartFile file) {

        if (StringUtils.isEmpty(token)) {
            LOGGER.error("Sesile token not provided");
            return new ResponseEntity<>("Missing token", HttpStatus.UNAUTHORIZED);
        }

        try {
            PesAller pes = pesAllerService.getByUuid(uuid);

            if (!token.equals(pesAllerService.getToken(pes))) {
                LOGGER.error("Invalid token for PES {}", pes.getUuid());
                return new ResponseEntity<>("Invalid token", HttpStatus.UNAUTHORIZED);
            }

            if (!status.equals("SIGNED") && !status.equals("WITHDRAWN") && !status.equals("DELETED")) {
                LOGGER.error("Invalid status ({}) incoming from Sesile for pes {}", status, uuid);
                return new ResponseEntity<>("Invalid status " + status, HttpStatus.BAD_REQUEST);
            }
            if (status.equals("SIGNED") && file == null) {
                LOGGER.error("PES {} signed but cannot find signed file", status, uuid);
                return new ResponseEntity<>("Missing signed file", HttpStatus.BAD_REQUEST);
            }

            sesileService.updatePesStatus(pes, status, file);
            return new ResponseEntity(HttpStatus.OK);

        } catch (PesNotFoundException e) {
            LOGGER.error("PES {} not found", uuid);
            return new ResponseEntity<>("PES not found", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            LOGGER.error("Failed to update PES {} status from Sesile: {}", uuid, e.getMessage());
            return new ResponseEntity<>("Stela error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

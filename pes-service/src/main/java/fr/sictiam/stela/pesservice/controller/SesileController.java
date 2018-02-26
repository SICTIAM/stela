package fr.sictiam.stela.pesservice.controller;

import fr.sictiam.stela.pesservice.model.LocalAuthority;
import fr.sictiam.stela.pesservice.model.SesileConfiguration;
import fr.sictiam.stela.pesservice.model.sesile.ServiceOrganisation;
import fr.sictiam.stela.pesservice.service.LocalAuthorityService;
import fr.sictiam.stela.pesservice.service.SesileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pes/sesile")
public class SesileController {

    private final SesileService sesileService;
    private final LocalAuthorityService localAuthorityService;

    public SesileController(SesileService sesileService, LocalAuthorityService localAuthorityService) {
        this.sesileService = sesileService;
        this.localAuthorityService = localAuthorityService;
    }

    @GetMapping("/organisations")
    public List<ServiceOrganisation> getCurrentOrganisations(
            @RequestAttribute("STELA-Current-Profile-UUID") String profileUuid,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid) throws Exception {
        LocalAuthority localAuthority = localAuthorityService.getByUuid(currentLocalAuthUuid);
        return sesileService.getServiceOrganisations(localAuthority, profileUuid);
    }

    @GetMapping("/organisations/{localAuthUuid}/{profileUuid}")
    public List<ServiceOrganisation> getCurrentOrganisationsByProfileUuid(@PathVariable String localAuthUuid,
            @PathVariable String profileUuid) throws Exception {
        LocalAuthority localAuthority = localAuthorityService.getByUuid(localAuthUuid);
        return sesileService.getServiceOrganisations(localAuthority, profileUuid);
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
}

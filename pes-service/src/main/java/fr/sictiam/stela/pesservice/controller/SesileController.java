package fr.sictiam.stela.pesservice.controller;

import fr.sictiam.stela.pesservice.model.SesileConfiguration;
import fr.sictiam.stela.pesservice.model.sesile.ServiceOrganisation;
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

    public SesileController(SesileService sesileService) {
        this.sesileService = sesileService;
    }

    @GetMapping("/organisations")
    public List<ServiceOrganisation> getCurrentOrganisations(
            @RequestAttribute("STELA-Current-Profile-UUID") String profileUuid) throws Exception {
        return sesileService.getServiceOrganisations(profileUuid);
    }

    @GetMapping("/organisations/{profileUuid}")
    public List<ServiceOrganisation> getCurrentOrganisationsByProfileUuid(@PathVariable String profileUuid)
            throws Exception {
        return sesileService.getServiceOrganisations(profileUuid);
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

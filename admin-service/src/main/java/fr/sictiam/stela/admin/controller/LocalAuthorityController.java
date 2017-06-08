package fr.sictiam.stela.admin.controller;

import fr.sictiam.stela.admin.model.LocalAuthority;
import fr.sictiam.stela.admin.model.Module;
import fr.sictiam.stela.admin.service.LocalAuthorityService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/admin/local-authority")
public class LocalAuthorityController {

    private final LocalAuthorityService localAuthorityService;

    public LocalAuthorityController(LocalAuthorityService localAuthorityService) {
        this.localAuthorityService = localAuthorityService;
    }

    @PostMapping
    public void create(@RequestBody @Valid LocalAuthority localAuthority) {
        localAuthorityService.create(localAuthority);
    }

    @PostMapping("/{uuid}/{module}")
    public void addModule(@PathVariable String uuid, @PathVariable Module module) {
        localAuthorityService.addModule(uuid, module);
    }

    @DeleteMapping("/{uuid}/{module}")
    public void removeModule(@PathVariable String uuid, @PathVariable Module module) {
        localAuthorityService.removeModule(uuid, module);
    }
}

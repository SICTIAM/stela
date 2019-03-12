package fr.sictiam.stela.admin.controller;

import fr.sictiam.stela.admin.model.Module;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/modules")
public class ModulesController {

    @GetMapping
    public Module[] getModules() {
        return Module.values();
    }
}

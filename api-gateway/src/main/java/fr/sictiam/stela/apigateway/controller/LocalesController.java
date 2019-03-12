package fr.sictiam.stela.apigateway.controller;

import fr.sictiam.stela.apigateway.service.LocalesService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/api-gateway/locales/")
public class LocalesController {

    private final LocalesService localesService;

    public LocalesController(LocalesService localesService) {
        this.localesService = localesService;
    }

    @GetMapping(value = "/{module}/{lng}/{ns}.json", produces = "application/json; charset=UTF-8")
    public String getJsonTranslation(@PathVariable String module, @PathVariable String lng, @PathVariable String ns) {
        return localesService.getJsonTranslation(module, lng, ns);
    }
}

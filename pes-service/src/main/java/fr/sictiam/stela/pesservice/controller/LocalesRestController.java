package fr.sictiam.stela.pesservice.controller;

import fr.sictiam.stela.pesservice.service.LocalesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/pes/locales")
public class LocalesRestController {

    @Autowired
    private LocalesService localesService;

    @GetMapping(value = "/{lng}/{ns}.json", produces = "application/json")
    public String getJsonTranslation(HttpServletResponse response, @PathVariable String lng, @PathVariable String ns) {
        return localesService.getJsonTranslation(lng, ns);
    }

}

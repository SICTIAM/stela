package fr.sictiam.stela.pesservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import fr.sictiam.stela.pesservice.service.LocalesService;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;

@RestController
@RequestMapping("/api/pes/locales")
public class LocalesRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalesRestController.class);
    
    @Autowired
    private LocalesService localesService;

    @GetMapping(value="/{lng}/{ns}.json", produces = "application/json")
    public String getJsonTranslation(HttpServletResponse response, @PathVariable String lng, @PathVariable String ns) {       
        return localesService.getJsonTranslation(lng, ns);
    }

}

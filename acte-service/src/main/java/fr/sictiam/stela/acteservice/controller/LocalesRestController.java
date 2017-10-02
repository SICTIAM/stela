package fr.sictiam.stela.acteservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;

@RestController
@RequestMapping("/api/acte/locales")
public class LocalesRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalesRestController.class);

    @GetMapping(value="/{lng}/{ns}.json", produces = "application/json")
    public String getJsonTranslation(HttpServletResponse response, @PathVariable String lng, @PathVariable String ns) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ClassPathResource resource = new ClassPathResource("/locales/" + lng + "/" + ns + ".json");
            FileCopyUtils.copy(resource.getInputStream(), bos);
        } catch (Exception e) {
            LOGGER.error("Unable to load json translation file: {}", e);
        }
        return bos.toString();
    }

}

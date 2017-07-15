package fr.sictiam.stela.apigateway.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ozwillo")
public class OzwilloController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OzwilloController.class);

    @PostMapping("/instance")
    public void createInstance() {
        LOGGER.debug("Gonna create a new instance");
    }
}

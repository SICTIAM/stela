package fr.sictiam.stela.apigateway.controller;

import fr.sictiam.stela.apigateway.service.RightsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/api-gateway/rights")
public class RightsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RightsController.class);

    private final RightsService rightsService;

    @Autowired
    public RightsController(RightsService rightsService) {
        this.rightsService = rightsService;
    }

    @GetMapping
    public List<String> getRights() {
        return rightsService.getRights();
    }
}

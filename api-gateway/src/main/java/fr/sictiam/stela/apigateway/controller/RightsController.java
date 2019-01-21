package fr.sictiam.stela.apigateway.controller;

import fr.sictiam.stela.apigateway.util.DiscoveryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/api-gateway/rights")
public class RightsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RightsController.class);

    @Autowired
    DiscoveryUtils discoveryUtils;

    @GetMapping
    public List<String> getRights() {
        RestTemplate restTemplate = new RestTemplate();

        List<String> notificationList = new ArrayList<>();

        try {
            notificationList.addAll(Arrays.asList(restTemplate.getForObject(discoveryUtils.acteServiceUrl() + "/api/acte/rights",
                    String[].class)));
        } catch (RuntimeException e) {
            LOGGER.warn("Module acte is probably not running : {}", e.getMessage());
        }

        try {
            notificationList.addAll(Arrays.asList(restTemplate.getForObject(discoveryUtils.convocationServiceUrl() +
                            "/api/convocation/rights",
                    String[].class)));
        } catch (RuntimeException e) {
            LOGGER.warn("Module convocation is probably not running : {}", e.getMessage());
        }

        try {
            notificationList.addAll(Arrays.asList(restTemplate.getForObject(discoveryUtils.pesServiceUrl() + "/api" +
                            "/pes/rights",
                    String[].class)));
        } catch (RuntimeException e) {
            LOGGER.warn("Module pes is probably not running : {}", e.getMessage());
        }

        return notificationList;
    }
}

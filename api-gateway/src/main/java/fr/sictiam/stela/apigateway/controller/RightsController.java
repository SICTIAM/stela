package fr.sictiam.stela.apigateway.controller;

import com.netflix.discovery.EurekaClient;
import fr.sictiam.stela.apigateway.util.DiscoveryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final EurekaClient discoveryClient;

    private final DiscoveryUtils discoveryUtils;

    public RightsController(EurekaClient discoveryClient, DiscoveryUtils discoveryUtils) {
        this.discoveryClient = discoveryClient;
        this.discoveryUtils = discoveryUtils;
    }

    @GetMapping
    public List<String> getRights() {
        RestTemplate restTemplate = new RestTemplate();

        List<String> rights = new ArrayList<>();

        try {
            if (discoveryClient.getApplication("acte-service") != null
                    && !discoveryClient.getApplication("acte-service").getInstances().isEmpty()) {
                rights.addAll(Arrays.asList(restTemplate.getForObject(discoveryUtils.acteServiceUrl() + "/api/acte/rights",
                        String[].class)));
            }
        } catch (RuntimeException e) {
            LOGGER.warn("Module acte is probably not running : {}", e.getMessage());
        }

        try {
            if (discoveryClient.getApplication("convocation-service") != null
                    && !discoveryClient.getApplication("convocation-service").getInstances().isEmpty()) {
                rights.addAll(Arrays.asList(restTemplate.getForObject(discoveryUtils.convocationServiceUrl() +
                                "/api/convocation/rights", String[].class)));
            }
        } catch (RuntimeException e) {
            LOGGER.warn("Module convocation is probably not running : {}", e.getMessage());
        }

        try {
            if (discoveryClient.getApplication("pes-service") != null
                    && !discoveryClient.getApplication("pes-service").getInstances().isEmpty()) {
                rights.addAll(Arrays.asList(restTemplate.getForObject(discoveryUtils.pesServiceUrl() + "/api" +
                                "/pes/rights", String[].class)));
            }
        } catch (RuntimeException e) {
            LOGGER.warn("Module pes is probably not running : {}", e.getMessage());
        }

        return rights;
    }
}

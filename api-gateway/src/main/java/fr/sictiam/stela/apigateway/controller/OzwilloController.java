package fr.sictiam.stela.apigateway.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@RestController
@RequestMapping("/ozwillo")
public class OzwilloController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OzwilloController.class);

    private final EurekaClient discoveryClient;

    public OzwilloController(EurekaClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    private String adminServiceUrl() {
        InstanceInfo instance = discoveryClient.getNextServerFromEureka("admin-service", false);
        return instance.getHomePageUrl();
    }

    @PostMapping("/instance")
    public void createInstance(@RequestAttribute String provisioningRequest) throws IOException {
        LOGGER.debug("Gonna create a new instance : {}", provisioningRequest);
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode provisioningRequestAsJson = mapper.readTree(provisioningRequest);
        ResponseEntity<Object> response =
                restTemplate.postForEntity(adminServiceUrl() + "/api/admin/local-authority", provisioningRequestAsJson, Object.class);
    }
}

package fr.sictiam.stela.apigateway.service;

import com.netflix.appinfo.InstanceInfo;
import fr.sictiam.stela.apigateway.util.DiscoveryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class RightsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RightsService.class);

    private final RestTemplate restTemplate;

    private final DiscoveryUtils discoveryUtils;

    private final ModulesService modulesService;

    @Autowired
    public RightsService(RestTemplateBuilder builder, ModulesService modulesService, DiscoveryUtils discoveryUtils) {
        this.restTemplate = builder.build();
        this.discoveryUtils = discoveryUtils;
        this.modulesService = modulesService;
    }

    public List<String> getRights() {
        List<String> rights = new ArrayList<>();

        modulesService.activeBusinessApplications().forEach(application -> {
            InstanceInfo instanceInfo = application.getInstances().get(0);
            String serviceName = modulesService.extractServiceName(application.getName());
            try {
                String[] response =
                        restTemplate.getForObject(this.buildRightsEndpointUrl(serviceName), String[].class);
                rights.addAll(Arrays.asList(Objects.requireNonNull(response)));
            } catch(RestClientException e) {
                LOGGER.warn("[getRights] An error occured in module {} : {}", application.getName(), e.getMessage());
            }
        });

        return rights;
    }

    private String buildRightsEndpointUrl(String applicationName) {
        return String.join("/", discoveryUtils.getServiceUrlByName(applicationName), "api", applicationName.toLowerCase(), "rights");
    }
}

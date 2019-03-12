package fr.sictiam.stela.apigateway.service;

import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import fr.sictiam.stela.apigateway.util.DiscoveryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ModulesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModulesService.class);

    private final EurekaClient discoveryClient;

    private final RestTemplate restTemplate;

    private final DiscoveryUtils discoveryUtils;

    public ModulesService(EurekaClient discoveryClient, RestTemplateBuilder restTemplateBuilder, DiscoveryUtils discoveryUtils) {
        this.discoveryClient = discoveryClient;
        this.restTemplate = restTemplateBuilder.build();
        this.discoveryUtils = discoveryUtils;
    }

    public List<String> getModules() {
        return restTemplate.getForObject(
                String.join("/", discoveryUtils.adminServiceUrl(), "api/admin/modules"),
                List.class);
    }

    public boolean moduleHaveInstance(String moduleName) {
        Application application = discoveryClient.getApplication(moduleName.concat("-service").toUpperCase());
        return application != null && !application.getInstances().isEmpty();
    }
}

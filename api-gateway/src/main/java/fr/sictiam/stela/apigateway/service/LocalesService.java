package fr.sictiam.stela.apigateway.service;

import fr.sictiam.stela.apigateway.util.DiscoveryUtils;
import fr.sictiam.stela.apigateway.util.LocalFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Service
public class LocalesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalesService.class);

    private final RestTemplate restTemplate;

    private final DiscoveryUtils discoveryUtils;

    private final ModulesService modulesService;


    public LocalesService(RestTemplateBuilder builder, DiscoveryUtils discoveryUtils, ModulesService modulesService) {
        this.restTemplate = builder.build();
        this.discoveryUtils = discoveryUtils;
        this.modulesService = modulesService;
    }

    public String getJsonTranslation(String module, String lng, String ns) {
        if (module.equals("api-gateway")) {
            return LocalFileUtils.getLocalFile(String.join("/","public/locales",lng, ns.concat(".json")));
        }

        if(modulesService.moduleHaveInstance(module)) {
            try {
                return Objects.requireNonNull(restTemplate.getForObject(this.buildLocalesEndpointUrl(module, lng, ns), String.class));
            } catch (RestClientException e) {
                LOGGER.warn("[getJsonTranslation] An error occured in module {} : {}", module, e.getMessage());
            }
        } else {
            LOGGER.warn("Module {} not have instance", module);
        }
        return "";
    }

    private String buildLocalesEndpointUrl(String applicationName, String lng, String ns) {
        return String.join("/", discoveryUtils.getServiceUrlByName(applicationName), "api", applicationName, "locales", lng, ns.concat(".json"));
    }
}

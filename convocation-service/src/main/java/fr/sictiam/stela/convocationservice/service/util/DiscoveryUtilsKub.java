package fr.sictiam.stela.convocationservice.service.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({ "integration", "prod", "test" })
public class DiscoveryUtilsKub implements DiscoveryUtils {

    @Value("${kub.services.admin}")
    private String adminUrl;

    @Override
    public String adminServiceUrl() {
        return adminUrl;
    }
}

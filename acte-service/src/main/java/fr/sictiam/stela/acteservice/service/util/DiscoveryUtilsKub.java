package fr.sictiam.stela.acteservice.service.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("kub")
public class DiscoveryUtilsKub implements DiscoveryUtils {

    @Value("${application.kub.services.admin}")
    private String adminUrl;

    @Override
    public String adminServiceUrl() {
        return adminUrl;
    }

}

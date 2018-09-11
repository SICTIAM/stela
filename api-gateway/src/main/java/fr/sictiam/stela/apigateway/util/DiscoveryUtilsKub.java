package fr.sictiam.stela.apigateway.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"integration", "prod", "atd24"})
public class DiscoveryUtilsKub implements DiscoveryUtils {

    @Value("${application.kub.services.admin}")
    private String adminUrl;

    @Value("${application.kub.services.pes}")
    private String pesUrl;

    @Value("${application.kub.services.acte}")
    private String acteUrl;

    @Value("${application.kub.services.convoc}")
    private String convocUrl;

    @Override
    public String adminServiceUrl() {
        return adminUrl;
    }

    @Override
    public String acteServiceUrl() {
        return acteUrl;
    }

    @Override
    public String pesServiceUrl() {
        return pesUrl;

    }

    @Override
    public String convocationServiceUrl() {
        return convocUrl;
    }

}

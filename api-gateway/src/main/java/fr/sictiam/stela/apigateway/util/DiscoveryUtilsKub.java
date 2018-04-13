package fr.sictiam.stela.apigateway.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({ "integration", "prod" })
public class DiscoveryUtilsKub implements DiscoveryUtils {

    @Value("${kub.services.admin}")
    private String adminUrl;

    @Value("${kub.services.pes}")
    private String pesUrl;

    @Value("${kub.services.acte}")
    private String acteUrl;

    @Value("${kub.services.admin.convoc}")
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
    public String convocServiceUrl() {
        return convocUrl;
    }

}

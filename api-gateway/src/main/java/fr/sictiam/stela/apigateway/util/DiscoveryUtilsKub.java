package fr.sictiam.stela.apigateway.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("kub")
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

    @Override
    public String getServiceUrlByName(String name) {
        switch (name.toLowerCase()) {
            case "pes": return pesUrl;
            case "acte": return acteUrl;
            case "admin": return adminUrl;
            case "convocation": return convocUrl;
            default: return null;
        }
    }
}

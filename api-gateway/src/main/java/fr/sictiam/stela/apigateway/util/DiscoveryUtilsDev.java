package fr.sictiam.stela.apigateway.util;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({ "dev", "dev-docker" })
public class DiscoveryUtilsDev implements DiscoveryUtils {

    private EurekaClient discoveryClient;

    public DiscoveryUtilsDev(EurekaClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @Override
    public String acteServiceUrl() {
        InstanceInfo instance = discoveryClient.getNextServerFromEureka("acte-service", false);
        return instance.getHomePageUrl();
    }

    @Override
    public String adminServiceUrl() {
        InstanceInfo instance = discoveryClient.getNextServerFromEureka("admin-service", false);
        return instance.getHomePageUrl();
    }

    @Override
    public String pesServiceUrl() {
        InstanceInfo instance = discoveryClient.getNextServerFromEureka("pes-service", false);
        return instance.getHomePageUrl();
    }

    @Override
    public String convocServiceUrl() {
        InstanceInfo instance = discoveryClient.getNextServerFromEureka("convocation-service", false);
        return instance.getHomePageUrl();
    }

}

package fr.sictiam.stela.apigateway.util;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!kub")
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
    public String convocationServiceUrl() {
        InstanceInfo instance = discoveryClient.getNextServerFromEureka("convocation-service", false);
        return instance.getHomePageUrl();
    }

    @Override
    public String getServiceUrlByName(String name) {
        InstanceInfo instance = discoveryClient.getNextServerFromEureka(name.concat("-service"), false);
        return instance.getHomePageUrl();
    }
}

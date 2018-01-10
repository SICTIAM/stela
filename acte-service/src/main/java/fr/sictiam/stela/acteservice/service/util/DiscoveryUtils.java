package fr.sictiam.stela.acteservice.service.util;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import org.springframework.stereotype.Component;

@Component
public class DiscoveryUtils {

    private static EurekaClient discoveryClient;

    public DiscoveryUtils(EurekaClient discoveryClient) {
        DiscoveryUtils.discoveryClient = discoveryClient;
    }

    public static String adminServiceUrl() {
        InstanceInfo instance = discoveryClient.getNextServerFromEureka("admin-service", false);
        return instance.getHomePageUrl();
    }
}

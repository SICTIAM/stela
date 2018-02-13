package fr.sictiam.stela.pesservice.service.util;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import fr.sictiam.stela.pesservice.config.NotTestCondition;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
@Conditional(NotTestCondition.class)
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

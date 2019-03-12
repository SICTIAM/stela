package fr.sictiam.stela.apigateway.util;

public interface DiscoveryUtils {

    String acteServiceUrl();

    String adminServiceUrl();

    String pesServiceUrl();

    String convocationServiceUrl();

    String getServiceUrlByName(String name);
}

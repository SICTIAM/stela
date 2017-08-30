package fr.sictiam.stela.apigateway.config;

import org.oasis_eu.spring.kernel.security.StaticOpenIdCConfiguration;

public class OpenIdConnectConfiguration extends StaticOpenIdCConfiguration {

    @Override
    public boolean requireAuthenticationForPath(String path) {
        return (path.contains("/api/") && !path.contains("/locales/") && !path.contains("/api/admin/ozwillo/instance")) || path.contains("/index.html");
    }
}

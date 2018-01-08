package fr.sictiam.stela.apigateway.config;

import fr.sictiam.stela.apigateway.model.LocalAuthorityInstance;
import fr.sictiam.stela.apigateway.service.LocalAuthorityInstanceService;
import fr.sictiam.stela.apigateway.util.SlugUtils;
import org.oasis_eu.spring.kernel.security.StaticOpenIdCConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.http.HttpServletRequest;

public class OpenIdConnectConfiguration extends StaticOpenIdCConfiguration {

    @Autowired
    private HttpServletRequest request;

    @Value("${application.urlWithSlug}")
    private String applicationUrlWithSlug;

    @Autowired
    private LocalAuthorityInstanceService localAuthorityInstanceService;

    @Override
    public boolean requireAuthenticationForPath(String path) {
        return (path.contains("/api/")
                && !path.contains("/locales/")
                && !path.contains("/api/admin/local-authority/all-basic")
                && !path.contains("/api/api-gateway/loginWithSlug")
                && !path.contains("/api/api-gateway/isLocalAuthorityInstance")
                && !path.contains("/api/admin/ozwillo"))
                || path.contains("/index.html");
    }

    @Override
    public String getClientId() {
        LocalAuthorityInstance localAuthorityInstance = localAuthorityInstanceService.findLocalAuthorityInstance();
        if (localAuthorityInstance != null) {
            return localAuthorityInstance.getClientId();
        } else {
            return "invalid";
        }
    }

    @Override
    public String getClientSecret() {
        LocalAuthorityInstance localAuthorityInstance = localAuthorityInstanceService.findLocalAuthorityInstance();
        if (localAuthorityInstance != null) {
            return localAuthorityInstance.getClientSecret();
        } else {
            return "invalid";
        }
    }

    @Override
    public String getCallbackUri() {
        return applicationUrlWithSlug.replace("%SLUG%", SlugUtils.getSlugNameFromRequest(request)) + "/callback";
    }
}

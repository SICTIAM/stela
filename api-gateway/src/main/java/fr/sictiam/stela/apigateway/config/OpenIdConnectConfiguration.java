package fr.sictiam.stela.apigateway.config;

import fr.sictiam.stela.apigateway.model.LocalAuthorityInstance;
import fr.sictiam.stela.apigateway.service.LocalAuthorityInstanceService;
import fr.sictiam.stela.apigateway.util.SlugUtils;
import org.oasis_eu.spring.kernel.security.StaticOpenIdCConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class OpenIdConnectConfiguration extends StaticOpenIdCConfiguration {

    @Value("${application.urlWithSlug}")
    private String applicationUrlWithSlug;

    @Autowired
    private LocalAuthorityInstanceService localAuthorityInstanceService;

    @Override
    public boolean skipAuthenticationForPath(String url) {
        return true;
    }

    @Override
    public boolean requireAuthenticationForPath(String path) {
        return (path.contains("/api/") && !path.contains("/locales/")
                && !path.contains("/api/admin/local-authority/all") && !path.contains("/api/api-gateway/loginWithSlug")
                && !path.contains("/api/api-gateway/isMainDomain") && !path.contains("/api/admin/ozwillo")
                && !path.contains("/api/admin/instance") && !path.contains("/api/acte/public"))
                && !path.contains("/ws/") && !path.contains("/externalws/") || path.contains("/index.html");
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
        RequestAttributes attribs = RequestContextHolder.getRequestAttributes();
        if (RequestContextHolder.getRequestAttributes() != null) {
            HttpServletRequest request = ((ServletRequestAttributes) attribs).getRequest();
            return applicationUrlWithSlug.replace("%SLUG%", SlugUtils.getSlugNameFromRequest(request)) + "/callback";
        }
        return null;
    }
}

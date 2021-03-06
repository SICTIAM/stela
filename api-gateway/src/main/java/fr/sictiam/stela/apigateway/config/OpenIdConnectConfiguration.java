package fr.sictiam.stela.apigateway.config;

import fr.sictiam.stela.apigateway.model.LocalAuthorityInstance;
import fr.sictiam.stela.apigateway.service.LocalAuthorityInstanceService;
import org.apache.commons.lang.StringUtils;
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

    @Value("${application.url}")
    private String applicationUrl;

    @Value("${application.portalUrl}")
    private String portalUrl;

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
                && !path.contains("/api/admin/instance") && !path.contains("/api/acte/public")
                && !path.contains("/editeur/api/") && !path.contains("/api/pes/sesile/signature-hook") && !path.contains("/actuator/")
                && !path.contains("/api/convocation"))
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
            String instanceId = request.getParameter("instance_id");
            StringBuilder callBackUri = new StringBuilder(applicationUrl);
            callBackUri.append("/callback");
            if (StringUtils.isNotBlank(instanceId)) {
                callBackUri.append("?instance_id=").append(instanceId);
            }
            return callBackUri.toString();
        }
        return null;
    }

    @Override
    public String getClaims() {
        RequestAttributes attribs = RequestContextHolder.getRequestAttributes();
        if (RequestContextHolder.getRequestAttributes() != null) {
            HttpServletRequest request = ((ServletRequestAttributes) attribs).getRequest();
            return request.getParameter("claims");
        }

        return null;
    }

    @Override
    public String getPostLogoutRedirectUri() {
        return portalUrl;
    }
}

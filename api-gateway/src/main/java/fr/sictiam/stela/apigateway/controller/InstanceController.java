package fr.sictiam.stela.apigateway.controller;

import fr.sictiam.stela.apigateway.model.Certificate;
import fr.sictiam.stela.apigateway.service.CertUtilService;
import fr.sictiam.stela.apigateway.service.LocalAuthorityInstanceService;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

@RestController
@RequestMapping("/api/api-gateway")
public class InstanceController {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstanceController.class);

    @Value("${application.urlWithSlug}")
    String applicationUrlWithSlug;

    @Value("${application.url}")
    String applicationUrl;

    @Autowired
    private LocalAuthorityInstanceService localAuthorityInstanceService;

    @Autowired
    private CertUtilService certUtilService;

    @GetMapping(value = "/loginWithSlug/{slugName}")
    public void loginWithSlug(@PathVariable String slugName, HttpServletResponse response, HttpServletRequest request)
            throws IOException {
        request.getSession().invalidate();
        StringBuilder loginUrlToRedirectTo = new StringBuilder(applicationUrl);
        loginUrlToRedirectTo.append("/login");
        if (StringUtils.isNotBlank(slugName)) {
            loginUrlToRedirectTo.append("?localAuthoritySlug=").append(slugName);
        }
        response.sendRedirect(loginUrlToRedirectTo.toString());
    }

    @GetMapping(value = "/certInfos")
    public Certificate getCertInfos(HttpServletRequest request) {
        return certUtilService.getCertInfosFromHeaders(request);
    }

    @GetMapping(value = "/isMainDomain")
    public boolean isMainDomain() {
        return localAuthorityInstanceService.findLocalAuthorityInstanceFromRequest() == null;
    }
}

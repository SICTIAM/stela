package fr.sictiam.stela.apigateway.controller;

import fr.sictiam.stela.apigateway.model.Certificate;
import fr.sictiam.stela.apigateway.model.LocalAuthorityInstance;
import fr.sictiam.stela.apigateway.service.CertUtilService;
import fr.sictiam.stela.apigateway.service.LocalAuthorityInstanceService;
import fr.sictiam.stela.apigateway.util.DiscoveryUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;
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

    @Autowired
    private DiscoveryUtils discoveryUtils;

    @GetMapping(value = "/loginWithSlug/{slugName}")
    public void loginWithSlug(@PathVariable String slugName, HttpServletResponse response, HttpServletRequest request)
            throws IOException {
        request.getSession().invalidate();
        StringBuilder loginUrlToRedirectTo = new StringBuilder(applicationUrl);
        loginUrlToRedirectTo.append("/login");
        if (StringUtils.isNotBlank(slugName)) {
            WebClient webClient = WebClient.create(discoveryUtils.adminServiceUrl());
            Mono<LocalAuthorityInstance> localAuthorityInstanceMono = webClient.get()
                    .uri("/api/admin/local-authority/instance/slug-name/{slugName}", slugName).retrieve()
                    .onStatus(HttpStatus::is4xxClientError, r -> Mono.empty())
                    .bodyToMono(LocalAuthorityInstance.class);
            LocalAuthorityInstance localAuthorityInstance = localAuthorityInstanceMono.block();

            loginUrlToRedirectTo.append("?instance_id=").append(localAuthorityInstance.getClientId());
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

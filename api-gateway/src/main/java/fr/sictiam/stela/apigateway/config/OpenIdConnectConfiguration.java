package fr.sictiam.stela.apigateway.config;

import fr.sictiam.stela.apigateway.model.LocalAuthorityInstance;
import fr.sictiam.stela.apigateway.util.SlugUtils;
import org.oasis_eu.spring.kernel.security.StaticOpenIdCConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Optional;

import static fr.sictiam.stela.apigateway.util.DiscoveryUtils.adminServiceUrl;

public class OpenIdConnectConfiguration extends StaticOpenIdCConfiguration {

    private static final String INSTANCE_KEY = "instance";

    @Autowired
    private HttpServletRequest request;

    @Override
    public boolean requireAuthenticationForPath(String path) {
        return (path.contains("/api/") && !path.contains("/locales/") && !path.contains("/api/admin/ozwillo/instance")) || path.contains("/index.html");
    }

    @Override
    public String getClientId() {
        LocalAuthorityInstance localAuthorityInstance = findLocalAuthorityInstance();
        if (localAuthorityInstance != null) {
            return localAuthorityInstance.getClientId();
        } else {
            return "invalid";
        }
    }

    @Override
    public String getClientSecret() {
        LocalAuthorityInstance localAuthorityInstance = findLocalAuthorityInstance();
        if (localAuthorityInstance != null) {
            return localAuthorityInstance.getClientSecret();
        } else {
            return "invalid";
        }
    }

    @Override
    public String getCallbackUri() {
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/callback";
    }

    private LocalAuthorityInstance findLocalAuthorityInstance() {

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(INSTANCE_KEY) != null) {
            return (LocalAuthorityInstance) session.getAttribute(INSTANCE_KEY);
        }

        String slugName = SlugUtils.getSlugNameFromRequest(request);

        WebClient webClient = WebClient.create(adminServiceUrl());
        Mono<LocalAuthorityInstance> localAuthorityInstanceMono = webClient.get()
                .uri("/api/admin/local-authority/instance/{slugName}", slugName)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response -> Mono.error(new RuntimeException("No local authority found for " + slugName)))
                .bodyToMono(LocalAuthorityInstance.class);


        Optional<LocalAuthorityInstance> optLocalAuthorityInstance = localAuthorityInstanceMono.blockOptional();
        if (optLocalAuthorityInstance.isPresent()) {
            session.setAttribute(INSTANCE_KEY, optLocalAuthorityInstance.get());
            return optLocalAuthorityInstance.get();
        }

        return null;
    }
}

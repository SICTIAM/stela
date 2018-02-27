package fr.sictiam.stela.apigateway.service;

import fr.sictiam.stela.apigateway.model.LocalAuthorityInstance;
import fr.sictiam.stela.apigateway.util.SlugUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static fr.sictiam.stela.apigateway.util.DiscoveryUtils.adminServiceUrl;

@Service
public class LocalAuthorityInstanceService {

    private static final String INSTANCE_KEY = "instance";

    public LocalAuthorityInstance findLocalAuthorityInstance() {
        RequestAttributes attribs = RequestContextHolder.getRequestAttributes();
        if (attribs instanceof NativeWebRequest) {
            HttpServletRequest request = (HttpServletRequest) ((NativeWebRequest) attribs).getNativeRequest();
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute(INSTANCE_KEY) != null) {
                return (LocalAuthorityInstance) session.getAttribute(INSTANCE_KEY);
            }
            LocalAuthorityInstance localAuthorityInstance = findLocalAuthorityInstanceFromRequest();
            if (localAuthorityInstance != null)
                session.setAttribute(INSTANCE_KEY, localAuthorityInstance);
            return localAuthorityInstance;
        }
        return null;

    }

    public LocalAuthorityInstance findLocalAuthorityInstanceFromRequest() {
        RequestAttributes attribs = RequestContextHolder.getRequestAttributes();
        if (attribs instanceof NativeWebRequest) {
            HttpServletRequest request = (HttpServletRequest) ((NativeWebRequest) attribs).getNativeRequest();
            String slugName = SlugUtils.getSlugNameFromRequest(request);
            WebClient webClient = WebClient.create(adminServiceUrl());
            Mono<LocalAuthorityInstance> localAuthorityInstanceMono = webClient.get()
                    .uri("/api/admin/local-authority/instance/{slugName}", slugName).retrieve()
                    .onStatus(HttpStatus::is4xxClientError, response -> Mono.empty())
                    .bodyToMono(LocalAuthorityInstance.class);

            LocalAuthorityInstance optLocalAuthorityInstance = localAuthorityInstanceMono.block();
            return optLocalAuthorityInstance;
        }
        return null;
    }
}

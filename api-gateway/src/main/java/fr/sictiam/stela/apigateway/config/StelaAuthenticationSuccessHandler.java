package fr.sictiam.stela.apigateway.config;

import static fr.sictiam.stela.apigateway.util.DiscoveryUtils.adminServiceUrl;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.oasis_eu.spring.kernel.security.OpenIdCAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.client.RestTemplate;

import fr.sictiam.stela.apigateway.model.Agent;
import fr.sictiam.stela.apigateway.model.StelaUserInfo;
import fr.sictiam.stela.apigateway.util.SlugUtils;

public class StelaAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(StelaAuthenticationSuccessHandler.class);

    private final String applicationUrlWithSlug;

    public StelaAuthenticationSuccessHandler(String applicationUrlWithSlug) {
        this.applicationUrlWithSlug = applicationUrlWithSlug;
    }
        
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        RestTemplate restTemplate = new RestTemplate();

        OpenIdCAuthentication authenticationOpen = (OpenIdCAuthentication) authentication;
        
        if (authenticationOpen.isAppAdmin() || authenticationOpen.isAppUser()) {
            LOGGER.debug("Authentication succeded and user authorized for this instance, returning to home page");

            Agent agent = new Agent(authenticationOpen.getUserInfo(), authenticationOpen.isAppAdmin(),
                    SlugUtils.getSlugNameFromRequest(request));
            ResponseEntity<String> agentProfile =
                    restTemplate.postForEntity(adminServiceUrl() + "/api/admin/agent", agent, String.class);

            String token = agentProfile.getBody();

            StelaUserInfo stelaUserInfo = StelaUserInfo.from(((OpenIdCAuthentication) authentication).getUserInfo());
            stelaUserInfo.setStelaToken(token);
            authenticationOpen.setUserInfo(stelaUserInfo);
            // Hard redirect on configured slugified application's URL
            // Kind of a hack since back end and front end are two different apps in dev profile
            //   and the backend has no other way to know where is the front end
            response.sendRedirect(applicationUrlWithSlug.replace("%SLUG%", SlugUtils.getSlugNameFromRequest(request)));
        } else {
            LOGGER.info("Authentication succeded but user not authorized for this instance !");
            response.sendError(401);
        }
    }
}

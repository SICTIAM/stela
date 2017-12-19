package fr.sictiam.stela.apigateway.config;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.oasis_eu.spring.kernel.security.OpenIdCAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.client.RestTemplate;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;

import fr.sictiam.stela.apigateway.model.Agent;
import fr.sictiam.stela.apigateway.model.StelaUserInfo;

public class StelaAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(StelaAuthenticationSuccessHandler.class);

    private final String applicationUrl;
        
    private final EurekaClient discoveryClient;

    //AuthenticationSuccessHandler defaultHandler = new SavedRequestAwareAuthenticationSuccessHandler();

    public StelaAuthenticationSuccessHandler(String applicationUrl, EurekaClient diClient) {
        this.applicationUrl = applicationUrl;
        this.discoveryClient=diClient;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        LOGGER.debug("Authentication succeded, returning to home page");

        RestTemplate restTemplate = new RestTemplate();        
        
        
        OpenIdCAuthentication authenticationOpen= (OpenIdCAuthentication) authentication;
        
        if(authenticationOpen.isAppAdmin() || authenticationOpen.isAppUser()) {
            Agent agent = new Agent(authenticationOpen.getUserInfo(), authenticationOpen.isAppAdmin());
            authenticationOpen.setUserInfo(StelaUserInfo.from(authenticationOpen.getUserInfo()));
            restTemplate.postForEntity(adminServiceUrl() + "/api/admin/agent", agent, Agent.class);
//            defaultHandler.onAuthenticationSuccess(request, response, authentication)

            // Hard redirect on configured applicationUrl
            // Kind of a hack since back end and front end are two different apps in dev profile
            //   and the backend has no other way to know where is the front end
            response.sendRedirect(applicationUrl);
        }else {
            response.sendError(401);
        }
       
    }
    
    private String adminServiceUrl() {
        InstanceInfo instance = discoveryClient.getNextServerFromEureka("admin-service", false);
        return instance.getHomePageUrl();
    }
}

package fr.sictiam.stela.apigateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class StelaAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(StelaAuthenticationSuccessHandler.class);

    private final String applicationUrl;

    //AuthenticationSuccessHandler defaultHandler = new SavedRequestAwareAuthenticationSuccessHandler();

    public StelaAuthenticationSuccessHandler(String applicationUrl) {
        this.applicationUrl = applicationUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        LOGGER.debug("Authentication succeded, returning to home page");

        // TODO : agent bootstrap in admin service
        // TODO : handle origin (ie before authentication) URL to forward on it instead

//        defaultHandler.onAuthenticationSuccess(request, response, authentication)

        // Hard redirect on configured applicationUrl
        // Kind of a hack since back end and front end are two different apps in dev profile
        //   and the backend has no other way to know where is the front end
        response.sendRedirect(applicationUrl);
    }
}

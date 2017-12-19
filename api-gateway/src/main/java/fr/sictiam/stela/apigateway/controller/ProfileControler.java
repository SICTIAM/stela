package fr.sictiam.stela.apigateway.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.oasis_eu.spring.kernel.security.OpenIdCAuthentication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.sictiam.stela.apigateway.model.StelaUserInfo;

@RestController
@RequestMapping("/api/api-gateway/")
public class ProfileControler {
    
    @Value("${application.url}")
    private String applicationUrl;

    @GetMapping(value="/switch/{profileUuid}")
    public void switchProfile(@PathVariable String profileUuid, HttpServletResponse response) throws IOException {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OpenIdCAuthentication authenticationOpen= (OpenIdCAuthentication) authentication;
        
        ((StelaUserInfo) authenticationOpen.getUserInfo()).setCurrentProfile(profileUuid);
        
        response.sendRedirect(applicationUrl);
    }

}


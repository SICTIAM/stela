package fr.sictiam.stela.apigateway.controller;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static fr.sictiam.stela.apigateway.util.DiscoveryUtils.adminServiceUrl;

@RestController
@RequestMapping("/api/api-gateway")
public class ProfileController {
    
    @Value("${application.urlWithSlug}")
    String applicationUrlWithSlug;

    @GetMapping(value="/switch/{profileUuid}")
    public void switchProfile(@PathVariable String profileUuid, HttpServletResponse response,
                              HttpServletRequest request) throws IOException {

        RestTemplate restTemplate = new RestTemplate();
        String slugForProfile =
                restTemplate.getForObject(adminServiceUrl() + "/api/admin/profile/{uuid}/slug", String.class, profileUuid);

        request.getSession().invalidate();

        String loginUrlToRedirectTo = applicationUrlWithSlug.replace("%SLUG%", slugForProfile) + "/login";

        response.sendRedirect(loginUrlToRedirectTo);
    }
}

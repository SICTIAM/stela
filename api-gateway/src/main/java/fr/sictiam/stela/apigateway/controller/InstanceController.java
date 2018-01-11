package fr.sictiam.stela.apigateway.controller;

import fr.sictiam.stela.apigateway.service.LocalAuthorityInstanceService;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
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

    @Autowired
    private LocalAuthorityInstanceService localAuthorityInstanceService;

    @GetMapping(value="/loginWithSlug/{slugName}")
    public void loginWithSlug(@PathVariable String slugName, HttpServletResponse response, HttpServletRequest request) throws IOException {
        request.getSession().invalidate();
        String loginUrlToRedirectTo = applicationUrlWithSlug.replace("%SLUG%", slugName) + "/login";
        response.sendRedirect(loginUrlToRedirectTo);
    }

    @GetMapping(value="/isMainDomain")
    public boolean isMainDomain() {
        return localAuthorityInstanceService.findLocalAuthorityInstanceFromRequest() == null;
    }
}

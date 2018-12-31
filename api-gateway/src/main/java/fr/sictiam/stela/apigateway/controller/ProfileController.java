package fr.sictiam.stela.apigateway.controller;

import fr.sictiam.stela.apigateway.model.AlertMessage;
import fr.sictiam.stela.apigateway.model.Notification;
import fr.sictiam.stela.apigateway.util.DiscoveryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/api-gateway")
public class ProfileController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileController.class);

    @Value("${application.url}")
    String applicationUrl;

    @Value("${application.portalUrl}")
    String portalUrl;

    @Autowired
    DiscoveryUtils discoveryUtils;

    @GetMapping(value = "/switch/{profileUuid}")
    public void switchProfile(@PathVariable String profileUuid, HttpServletResponse response,
            HttpServletRequest request) throws IOException {

        RestTemplate restTemplate = new RestTemplate();
        String instanceId = restTemplate.getForObject(
                discoveryUtils.adminServiceUrl() + "/api/admin/profile/{uuid}/instance-id", String.class, profileUuid);

        request.getSession().invalidate();

        String loginUrlToRedirectTo = applicationUrl + "/login?instance_id=" + instanceId;

        response.sendRedirect(loginUrlToRedirectTo);
    }

    @GetMapping("/ozwillo-portal/my/profile")
    public void redirectOzwilloProfile(HttpServletResponse response) throws IOException {
        response.sendRedirect(portalUrl + "my/profile");
    }

    @GetMapping("/profile/all-notifications")
    public List<Notification> getAllNotifications() {
        RestTemplate restTemplate = new RestTemplate();

        List<Notification> notificationFiltered = new ArrayList<>();

        try {
            notificationFiltered.addAll(mapNotififaction("ACTES_",
                    restTemplate.getForObject(discoveryUtils.acteServiceUrl() + "/api/acte/notifications/all",
                            Notification[].class)));
        } catch (RuntimeException e) {
            LOGGER.warn("Module acte is probably not running : {}", e.getMessage());
        }

        try {
            notificationFiltered.addAll(mapNotififaction("PES_",
                    restTemplate.getForObject(discoveryUtils.pesServiceUrl() + "/api/pes/notifications/all",
                            Notification[].class)));
        } catch (RuntimeException e) {
            LOGGER.warn("Module pes is probably not running : {}", e.getMessage());
        }

        try {
            notificationFiltered.addAll(mapNotififaction("CONVOCATION_",
                    restTemplate.getForObject(discoveryUtils.acteServiceUrl() + "/api/convocation/notifications/all",
                            Notification[].class)));
        } catch (RuntimeException e) {
            LOGGER.warn("Module convocation is probably not running : {}", e.getMessage());
        }

        return notificationFiltered;
    }


    private List<Notification> mapNotififaction(String prefix, Notification[] notifications) {
        return Arrays.asList(notifications).stream()
                .map(notification -> new Notification(prefix + notification.getType(),
                        notification.isDeactivatable(), notification.isDefaultValue()))
                .collect(Collectors.toList());
    }

    @GetMapping("/alert-messages")
    public Map<String, AlertMessage> getAlertMessageModules() {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, AlertMessage> alertMessageModules = new HashMap<>();
        AlertMessage alertMessageActe = restTemplate
                .getForObject(discoveryUtils.acteServiceUrl() + "/api/acte/admin/alert-message", AlertMessage.class);
        alertMessageModules.put("actes", alertMessageActe);
        AlertMessage alertMessagePes = restTemplate
                .getForObject(discoveryUtils.pesServiceUrl() + "/api/pes/admin/alert-message", AlertMessage.class);
        alertMessageModules.put("pes", alertMessagePes);

        return alertMessageModules;
    }
}

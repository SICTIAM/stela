package fr.sictiam.stela.apigateway.controller;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import fr.sictiam.stela.apigateway.model.Agent;
import fr.sictiam.stela.apigateway.model.AlertMessage;
import fr.sictiam.stela.apigateway.model.Notification;
import fr.sictiam.stela.apigateway.model.StelaUserInfo;
import fr.sictiam.stela.apigateway.service.ModulesService;
import fr.sictiam.stela.apigateway.util.DiscoveryUtils;
import org.oasis_eu.spring.kernel.security.OpenIdCAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
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

    @Autowired
    ModulesService modulesService;

    @Autowired
    EurekaClient discoveryClient;

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

        modulesService.activeBusinessApplications().forEach(application -> {
            // To adapt when there will be multiple instances per service
            InstanceInfo instanceInfo = application.getInstances().get(0);
            String serviceName = modulesService.extractServiceName(application.getName());
            String applicationName = instanceInfo.getMetadata().get("name");
            try {
                notificationFiltered.addAll(mapNotififaction(applicationName.concat("_"),
                        restTemplate.getForObject(this.buildNotificationEndpointUrl(serviceName), Notification[].class)));
            } catch(RestClientException e) {
                LOGGER.error("[getAllNotifications] An error occured in module {} : {}", serviceName, e.getMessage());
            }
        });

        return notificationFiltered;
    }


    private List<Notification> mapNotififaction(String prefix, Notification[] notifications) {
        return Arrays.stream(notifications)
                .map(notification -> new Notification(prefix + notification.getType(),
                        notification.isDeactivatable(), notification.isDefaultValue()))
                .collect(Collectors.toList());
    }

    @GetMapping("/alert-messages")
    public Map<String, AlertMessage> getAlertMessageModules() {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, AlertMessage> alertMessageModules = new HashMap<>();

        modulesService.activeBusinessApplications().forEach(application -> {
            // To adapt when there will be multiple instances per service
            InstanceInfo instanceInfo = application.getInstances().get(0);
            String applicationName = instanceInfo.getMetadata().get("name").toLowerCase();
            String serviceName = modulesService.extractServiceName(application.getName());
            try {
                AlertMessage alertMessageActe = restTemplate
                        .getForObject(this.buildAlerteMessageEndpointUrl(serviceName), AlertMessage.class);
                alertMessageModules.put(applicationName, alertMessageActe);
            } catch(RestClientException e) {
                LOGGER.error("[getAlertMessageModules] An error occured in module {} : {}", applicationName, e.getMessage());
            }
        });

        return alertMessageModules;
    }

    @GetMapping("/profile/{profileUuid}/update-jwt")
    public ResponseEntity updateJwt(@PathVariable String profileUuid) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OpenIdCAuthentication authenticationOpen = (OpenIdCAuthentication) authentication;

        if (authenticationOpen.isAppAdmin() || authenticationOpen.isAppUser()) {
            RestTemplate restTemplate = new RestTemplate();
            String instanceId = restTemplate.getForObject(
                    discoveryUtils.adminServiceUrl() + "/api/admin/profile/{uuid}/instance-id", String.class, profileUuid);
            Agent agent = new Agent(authenticationOpen.getUserInfo(), authenticationOpen.isAppAdmin(), instanceId);
            ResponseEntity<String> agentProfile = restTemplate
                    .postForEntity(discoveryUtils.adminServiceUrl() + "/api/admin/agent", agent, String.class);


            String token = agentProfile.getBody();
            StelaUserInfo stelaUserInfo = StelaUserInfo.from(((OpenIdCAuthentication) authentication).getUserInfo());
            stelaUserInfo.setStelaToken(token);
            authenticationOpen.setUserInfo(stelaUserInfo);
            return new ResponseEntity(HttpStatus.OK);
        } else {
            LOGGER.error("Authentication succeded but user not authorized for this instance !");
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
    }

    private String buildNotificationEndpointUrl(String applicationName) {
        return String.join("/", discoveryUtils.getServiceUrlByName(applicationName), "api", applicationName.toLowerCase(), "notifications/all");
    }

    private String buildAlerteMessageEndpointUrl(String applicationName) {
        return String.join("/", discoveryUtils.getServiceUrlByName(applicationName), "api", applicationName.toLowerCase(), "admin/alert-message");
    }
}

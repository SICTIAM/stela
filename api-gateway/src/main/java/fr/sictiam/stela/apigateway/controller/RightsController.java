package fr.sictiam.stela.apigateway.controller;

import fr.sictiam.stela.apigateway.util.DiscoveryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/api-gateway/rights")
public class RightsController {

    @Autowired
    DiscoveryUtils discoveryUtils;

    @GetMapping
    public List<String> getRights() {
        RestTemplate restTemplate = new RestTemplate();
        String[] acteRights = restTemplate.getForObject(discoveryUtils.acteServiceUrl() + "/api/acte/rights",
                String[].class);

        String[] pesRights = restTemplate.getForObject(discoveryUtils.pesServiceUrl() + "/api/pes/rights",
                String[].class);

        List<String> notificationList = new ArrayList<>();
        notificationList.addAll(Arrays.asList(acteRights));
        notificationList.addAll(Arrays.asList(pesRights));

        return notificationList;
    }

}

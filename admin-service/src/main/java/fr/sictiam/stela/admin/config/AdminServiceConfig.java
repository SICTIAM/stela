package fr.sictiam.stela.admin.config;

import fr.sictiam.stela.admin.config.filter.LogRequestResponseFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Configuration
public class AdminServiceConfig {

    @Autowired
    LogRequestResponseFilter logRequestResponseFilter;

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(logRequestResponseFilter));
        return restTemplate;
    }
}

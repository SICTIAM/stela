package fr.sictiam.stela.apigateway.config;

import fr.sictiam.stela.apigateway.config.filter.AuthorizationHeaderFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZuulConfig {

    @Bean
    public AuthorizationHeaderFilter authorizationHeaderFilter() {
        return new AuthorizationHeaderFilter();
    }
}

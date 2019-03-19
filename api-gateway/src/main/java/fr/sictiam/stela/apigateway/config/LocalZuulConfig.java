package fr.sictiam.stela.apigateway.config;

import fr.sictiam.stela.apigateway.config.filter.LocalAuthorizationHeaderFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("test-e2e")
@Configuration
public class LocalZuulConfig {

    @Bean
    public LocalAuthorizationHeaderFilter authorizationHeaderFilter() {
        return new LocalAuthorizationHeaderFilter();
    }
}

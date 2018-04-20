package fr.sictiam.stela.apigateway.config;

import fr.sictiam.stela.apigateway.config.filter.AuthorizationHeaderFilter;
import fr.sictiam.stela.apigateway.config.filter.PreLoggingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZuulConfig {

    // @Bean
    // public ZuulFallbackProvider zuulFallbackProvider() {
    // return new DefaultFallbackProvider();
    // }

    @Bean
    public PreLoggingFilter preLoggingFilter() {
        return new PreLoggingFilter();
    }

    @Bean
    public AuthorizationHeaderFilter authorizationHeaderFilter() {
        return new AuthorizationHeaderFilter();
    }
}

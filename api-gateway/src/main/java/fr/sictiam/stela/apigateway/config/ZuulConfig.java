package fr.sictiam.stela.apigateway.config;

import org.springframework.cloud.netflix.zuul.filters.route.ZuulFallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZuulConfig {

    @Bean
    public ZuulFallbackProvider zuulFallbackProvider() {
        return new DefaultFallbackProvider();
    }
}

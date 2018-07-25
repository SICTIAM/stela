package fr.sictiam.stela.apigateway.config;

import org.oasis_eu.spring.kernel.security.TokenRefreshInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenRefreshInterceptor());
    }

    @Bean
    public TokenRefreshInterceptor tokenRefreshInterceptor() {
        return new TokenRefreshInterceptor();
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/pes/**").setViewName("forward:/index.html");
        registry.addViewController("/actes/**").setViewName("forward:/index.html");
        registry.addViewController("/admin/**").setViewName("forward:/index.html");
        registry.addViewController("/profil").setViewName("forward:/index.html");
        registry.addViewController("/callback").setViewName("forward:/index.html");
        registry.addViewController("/mentions-legales").setViewName("forward:/index.html");
        registry.addViewController("/registre-des-deliberations/**").setViewName("forward:/index.html");
    }
}

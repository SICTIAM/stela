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

    private final String localAuthority = "{localAuthoritySlug:^(?!api|login|logout)[\\w-]*}";

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/" + localAuthority).setViewName("forward:/index.html");
        registry.addViewController("/" + localAuthority + "/pes/**").setViewName("forward:/index.html");
        registry.addViewController("/" + localAuthority + "/actes/**").setViewName("forward:/index.html");
        registry.addViewController("/" + localAuthority + "/admin/**").setViewName("forward:/index.html");
        registry.addViewController("/" + localAuthority + "/profil").setViewName("forward:/index.html");
        registry.addViewController("/" + localAuthority + "/callback").setViewName("forward:/index.html");
        registry.addViewController("/mentions-legales").setViewName("forward:/index.html");
        registry.addViewController("/" + localAuthority + "/mentions-legales").setViewName("forward:/index.html");
        registry.addViewController("/registre-des-deliberations/**").setViewName("forward:/index.html");
        registry.addViewController("/" + localAuthority + "/registre-des-deliberations/**").setViewName("forward:/index.html");
        registry.addViewController("/choix-collectivite").setViewName("forward:/index.html");
    }
}

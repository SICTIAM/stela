package fr.sictiam.stela.apigateway.config;

import org.oasis_eu.spring.kernel.security.TokenRefreshInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

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

    private final String localAuthority = "{localAuthoritySlug:^(?!api|editeur|login|logout)[\\w-]*}";

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/" + localAuthority).setViewName("forward:/index.html");
        registry.addViewController("/" + localAuthority + "/pes/**").setViewName("forward:/index.html");
        registry.addViewController("/" + localAuthority + "/actes/**").setViewName("forward:/index.html");
        registry.addViewController("/" + localAuthority + "/admin/**").setViewName("forward:/index.html");
        registry.addViewController("/" + localAuthority + "/convocation/**").setViewName("forward:/index.html");
        registry.addViewController("/" + localAuthority + "/profil").setViewName("forward:/index.html");
        registry.addViewController("/" + localAuthority + "/callback").setViewName("forward:/index.html");
        registry.addViewController("/mentions-legales").setViewName("forward:/index.html");
        registry.addViewController("/" + localAuthority + "/mentions-legales").setViewName("forward:/index.html");
        registry.addViewController("/registre-des-deliberations/**").setViewName("forward:/index.html");
        registry.addViewController("/" + localAuthority + "/registre-des-deliberations/**").setViewName("forward:/index.html");
        registry.addViewController("/choix-collectivite").setViewName("forward:/index.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/public/static/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS));

        registry.addResourceHandler("/img/**")
                .addResourceLocations("classpath:/public/img/")
                .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS));
    }

}

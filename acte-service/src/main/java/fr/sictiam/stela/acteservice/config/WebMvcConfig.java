package fr.sictiam.stela.acteservice.config;

import fr.sictiam.stela.acteservice.interceptor.CertificateInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableWebMvc
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Bean
    CertificateInterceptor certificateInterceptor() {
        return new CertificateInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(certificateInterceptor()).addPathPatterns("/editeur/**");
    }

}
package fr.sictiam.stela.acteservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@Configuration
public class ThymeleafConfiguration {
    @Bean
    public ClassLoaderTemplateResolver templateResolver() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/");
        templateResolver.setTemplateMode("HTML5");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("XHTML");
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setOrder(1);
        return templateResolver;
    }
}

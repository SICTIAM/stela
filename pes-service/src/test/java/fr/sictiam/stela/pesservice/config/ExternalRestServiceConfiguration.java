package fr.sictiam.stela.pesservice.config;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import fr.sictiam.stela.pesservice.service.ExternalRestService;

@Profile("test")
@Configuration
public class ExternalRestServiceConfiguration {
    @Bean
    @Primary
    public ExternalRestService externalRestService() {
        return Mockito.mock(ExternalRestService.class);
    }
}
package fr.sictiam.stela.convocationservice.config;

import fr.sictiam.stela.convocationservice.service.ExternalRestService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("test")
@Configuration
public class ExternalRestServiceConfiguration {
    @Bean
    @Primary
    public ExternalRestService externalRestService() {
        return Mockito.mock(ExternalRestService.class);
    }
}
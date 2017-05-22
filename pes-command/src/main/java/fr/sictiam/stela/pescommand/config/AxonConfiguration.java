package fr.sictiam.stela.pescommand.config;

import org.axonframework.amqp.eventhandling.RoutingKeyResolver;
import org.axonframework.serialization.json.JacksonSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AxonConfiguration {

    @Bean
    JacksonSerializer axonJsonSerializer() {
        return new JacksonSerializer();
    }

    @Bean
    RoutingKeyResolver routingKeyResolver() {
        return new PesRoutingKeyResolver();
    }
}

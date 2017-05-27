package fr.sictiam.stela.pescommand.config;

import fr.sictiam.stela.pescommand.aggregate.PesAggregate;
import fr.sictiam.stela.pescommand.aggregate.PesAggregateCommandHandler;
import org.axonframework.amqp.eventhandling.RoutingKeyResolver;
import org.axonframework.serialization.json.JacksonSerializer;
import org.axonframework.spring.config.AxonConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AxonConfig {

    @Autowired
    private AxonConfiguration axonConfiguration;

    @Bean
    JacksonSerializer axonJsonSerializer() {
        return new JacksonSerializer();
    }

    @Bean
    RoutingKeyResolver routingKeyResolver() {
        return new PesRoutingKeyResolver();
    }

    @Bean
    public PesAggregateCommandHandler pesAggregateCommandHandler() {
        return new PesAggregateCommandHandler(axonConfiguration.repository(PesAggregate.class));
    }
}

package fr.sictiam.stela.pescommand.config;

import fr.sictiam.stela.pescommand.event.PesArCreatedEvent;
import fr.sictiam.stela.pescommand.event.PesCreatedEvent;
import fr.sictiam.stela.pescommand.event.PesSentEvent;
import org.axonframework.amqp.eventhandling.RoutingKeyResolver;
import org.axonframework.eventhandling.EventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

public class PesRoutingKeyResolver implements RoutingKeyResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(PesRoutingKeyResolver.class);

    @Value("${application.amqp.pes.createdKey}")
    private String createdKey;
    @Value("${application.amqp.pes.sentDgfipKey}")
    private String sentDgfipKey;
    @Value("${application.amqp.pes.receivedArKey}")
    private String receivedArKey;

    @Override
    public String resolveRoutingKey(EventMessage<?> eventMessage) {
        LOGGER.debug("Received an event to route : {}", eventMessage.getIdentifier());
        if (eventMessage.getPayload() instanceof PesCreatedEvent)
            return createdKey;
        // not sure the next are needed
        if (eventMessage.getPayload() instanceof PesSentEvent)
            return sentDgfipKey;
        if (eventMessage.getPayload() instanceof PesArCreatedEvent)
            return receivedArKey;
        return "#";
    }
}

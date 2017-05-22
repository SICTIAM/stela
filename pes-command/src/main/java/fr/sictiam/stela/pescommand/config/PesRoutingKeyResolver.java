package fr.sictiam.stela.pescommand.config;

import fr.sictiam.stela.pescommand.event.PesCreatedEvent;
import org.axonframework.amqp.eventhandling.RoutingKeyResolver;
import org.axonframework.eventhandling.EventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PesRoutingKeyResolver implements RoutingKeyResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(PesRoutingKeyResolver.class);

    @Override
    public String resolveRoutingKey(EventMessage<?> eventMessage) {
        LOGGER.debug("Received an event to route : {}", eventMessage.getIdentifier());
        if (eventMessage.getPayload() instanceof PesCreatedEvent)
            return "pes.created";
        return "#";
    }
}

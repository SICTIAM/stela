package fr.sictiam.stela.pes.dgfip.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sictiam.stela.pes.dgfip.model.event.PesCreatedEvent;
import fr.sictiam.stela.pes.dgfip.model.event.PesSentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ReceiverService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReceiverService.class);

    @Value("${application.amqp.pes.sentDgfipKey}")
    private String sentDgfipKey;

    @Value("${application.amqp.pes.exchange}")
    private String exchange;

    private final AmqpTemplate amqpTemplate;

    public ReceiverService(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue,
            exchange = @Exchange(value = "#{'${application.amqp.pes.exchange}'}", type = ExchangeTypes.TOPIC, durable = "true"),
            key = "#{'${application.amqp.pes.createdKey}'}")
    )
    public void processIncomingPes(Message message) {
        LOGGER.debug("Received a PES message {}", message);
        // TODO : Find a way to have automatic parsing from method arguments
        // Problem comes from AMQP message having contentType set to null
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            PesCreatedEvent pesCreatedEvent = objectMapper.readValue(message.getBody(), PesCreatedEvent.class);
            LOGGER.debug("Parsed the new PES : {}", pesCreatedEvent.toString());
            PesSentEvent pesSentEvent = new PesSentEvent(pesCreatedEvent.getPesUuid(), "pes-dgfip-sender", pesCreatedEvent.getEventDate());
            amqpTemplate.convertAndSend(exchange, sentDgfipKey, pesSentEvent);
        } catch (IOException e) {
            LOGGER.error("Unable to parse incoming PES", e);
        }
    }
}

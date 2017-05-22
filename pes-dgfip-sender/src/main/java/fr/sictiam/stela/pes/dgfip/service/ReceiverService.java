package fr.sictiam.stela.pes.dgfip.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sictiam.stela.pes.dgfip.model.Pes;
import fr.sictiam.stela.pes.dgfip.model.PesAr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ReceiverService {

    @Value("${spring.application.exchange}")
    private String exchangeName;

    private final AmqpTemplate amqpTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(ReceiverService.class);

    public ReceiverService(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    @RabbitListener(queues = "pes.queue")
    public void processIncomingPes(Message message) {
        LOGGER.debug("*************");
        // TODO : Find a way to have automatic parsing from method arguments
        // Problem comes from AMQP message having contentType set to null
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Pes pes = objectMapper.readValue(message.getBody(), Pes.class);
            LOGGER.debug("Received a new PES : {}", pes.toString());

            amqpTemplate.convertAndSend("pesAr.exchange", "#", new PesAr(pes.getPesId()));
        } catch (IOException e) {
            LOGGER.error("Unable to parse incoming PES", e);
        }

    }
}

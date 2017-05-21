package fr.sictiam.stela.pes.dgfip.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sictiam.stela.pes.dgfip.model.Pes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ReceiverService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReceiverService.class);

    @RabbitListener(queues = "#{'${spring.application.queue}'}")
    public void processIncomingPes(Message message) {
        // TODO : Find a way to have automatic parsing from method arguments
        // Problem comes from AMQP message having contentType set to null
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Pes pes = objectMapper.readValue(message.getBody(), Pes.class);
            LOGGER.debug("Received a new PES : {}", pes.toString());
        } catch (IOException e) {
            LOGGER.error("Unable to parse incoming PES", e);
        }
    }
}

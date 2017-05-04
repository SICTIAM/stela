package fr.sictiam.stela.pes.dgfip.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class ReceiverService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReceiverService.class);

    @RabbitListener(queues = "#{'${spring.application.queue}'}")
    public void processIncomingPes(Message message) {
        LOGGER.debug("Received a message : {}", message.toString());
        LOGGER.debug("\twith body : {}", new String(message.getBody()));
    }
}

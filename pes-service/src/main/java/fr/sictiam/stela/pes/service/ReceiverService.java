package fr.sictiam.stela.pes.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sictiam.stela.pes.model.event.PesArReceivedEvent;
import fr.sictiam.stela.pes.model.event.PesSentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ReceiverService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReceiverService.class);

    @Autowired
    private PesEventService pesEventService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue,
            exchange = @Exchange(value = "#{'${application.amqp.pes.exchange}'}", type = ExchangeTypes.TOPIC, durable = "true"),
            key = "#{'${application.amqp.pes.receivedArKey}'}")
    )
    public void processPesAr(Message message) {
        LOGGER.debug("Received a message PES AR: {}", message.toString());
        LOGGER.debug("\twith body : {}", new String(message.getBody()));
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            PesArReceivedEvent pesArReceivedEvent = objectMapper.readValue(message.getBody(), PesArReceivedEvent.class);
            LOGGER.debug("Received a new PES AR command : {} {} {}", pesArReceivedEvent.getPesId(), pesArReceivedEvent.getFileContent(), pesArReceivedEvent.getFileName());
            pesEventService.save(pesArReceivedEvent);
        } catch (IOException e) {
            LOGGER.error("Unable to parse incoming PES AR", e);
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue,
            exchange = @Exchange(value = "#{'${application.amqp.pes.exchange}'}", type = ExchangeTypes.TOPIC, durable = "true"),
            key = "#{'${application.amqp.pes.sentDgfipKey}'}")
    )
    public void processPesSent(Message message) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            PesSentEvent pesSentEvent = objectMapper.readValue(message.getBody(), PesSentEvent.class);
            LOGGER.debug("Received a new PES Send command: {} ({})", pesSentEvent.getPesId(), pesSentEvent.getEventDate());
            //pesEventService.save(pesSentEvent); // TODO: FIX ME !
        } catch (IOException e) {
            LOGGER.error("Unable to parse incoming PES Send", e);
        }
    }
}

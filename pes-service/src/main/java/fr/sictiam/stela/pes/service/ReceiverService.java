package fr.sictiam.stela.pes.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sictiam.stela.pes.model.PesHistory;
import fr.sictiam.stela.pes.model.StatusType;
import fr.sictiam.stela.pes.model.event.PesAckEvent;
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
    private PesHistoryService pesHistoryService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue,
            exchange = @Exchange(value = "#{'${application.amqp.pes.exchange}'}", type = ExchangeTypes.TOPIC, durable = "true"),
            key = "#{'${application.amqp.pes.receivedAckKey}'}")
    )
    public void processPesAr(Message message) {
        LOGGER.debug("Received a message PES ACK: {}", message.toString());
        LOGGER.debug("\twith body: {}", new String(message.getBody()));
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            PesAckEvent pesAckEvent = objectMapper.readValue(message.getBody(), PesAckEvent.class);
            LOGGER.debug("Received a new PES ACK: {} {} {}", pesAckEvent.getPesUuid(), pesAckEvent.getOrigin(), pesAckEvent.getEventDate());
            pesHistoryService.create(new PesHistory(pesAckEvent.getPesUuid(), StatusType.ACK_RECEIVED, pesAckEvent.getOrigin(), pesAckEvent.getEventDate()));
        } catch (IOException e) {
            LOGGER.error("Unable to parse incoming PES ACK", e);
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
            LOGGER.debug("Received a new PES Sent event: {} ({})", pesSentEvent.getPesUuid(), pesSentEvent.getEventDate());
            pesHistoryService.create(new PesHistory(pesSentEvent.getPesUuid(), StatusType.SENT, pesSentEvent.getOrigin(), pesSentEvent.getEventDate()));
        } catch (IOException e) {
            LOGGER.error("Unable to parse incoming PES Send", e);
        }
    }
}

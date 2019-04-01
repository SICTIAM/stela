package fr.sictiam.stela.acteservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sictiam.stela.acteservice.model.event.Event;
import fr.sictiam.stela.acteservice.model.event.LocalAuthorityEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(value = "application.amqp.enabled")
public class ReceiverService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReceiverService.class);

    @Autowired
    private LocalAuthorityService localAuthorityService;

    @RabbitListener(bindings = @QueueBinding(value = @Queue(name = "acteQueue"),
            exchange = @Exchange(value = "#{'${application.amqp.admin.exchange}'}", type = ExchangeTypes.FANOUT)))
    public void fromAdminService(Message message) {
        LOGGER.debug("Received a message {}", message);

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            Event event = objectMapper.readValue(message.getBody(), Event.class);

            if (event instanceof LocalAuthorityEvent) {
                localAuthorityService.handleEvent((LocalAuthorityEvent) event);
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }
}

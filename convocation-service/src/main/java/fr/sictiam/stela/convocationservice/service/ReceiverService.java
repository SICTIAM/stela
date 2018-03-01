package fr.sictiam.stela.convocationservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sictiam.stela.convocationservice.model.event.Event;
import fr.sictiam.stela.convocationservice.model.event.LocalAuthorityEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(value = "application.rabbit.enabled")
public class ReceiverService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReceiverService.class);

	@Value("${application.amqp.convocation.createdKey}")
	private String sentDgfipKey;

	@Value("${application.amqp.convocation.exchange}")
	private String exchange;

	@Autowired
	private AmqpTemplate amqpTemplate;

	@Autowired
	private LocalAuthorityService localAuthorityService;

	@RabbitListener(bindings = @QueueBinding(value = @Queue(name = "acteQueue", durable = "true"), exchange = @Exchange(value = "#{'${application.amqp.convocation.exchange}'}", type = ExchangeTypes.FANOUT, durable = "true"), key = "#{'${application.amqp.acte.adminKey}'}"))
	public void fromAdminService(Message message) {
		LOGGER.debug("Received a message {}", message);

		ObjectMapper objectMapper = new ObjectMapper();

		try {
			Event event = objectMapper.readValue(message.getBody(), Event.class);
			LOGGER.debug(event.getOrigin());

			if (event instanceof LocalAuthorityEvent) {
				localAuthorityService.handleEvent((LocalAuthorityEvent) event);
			}

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		// amqpTemplate.convertAndSend(exchange, sentDgfipKey, message);
	}
}

package fr.sictiam.stela.pescommand.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sictiam.stela.pescommand.command.CreatePesArCommand;
import fr.sictiam.stela.pescommand.command.SendPesCommand;
import org.axonframework.commandhandling.CommandBus;
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

import static org.axonframework.commandhandling.GenericCommandMessage.asCommandMessage;

@Service
public class ReceiverService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReceiverService.class);

    private final CommandBus commandBus;

    @Autowired
    public ReceiverService(CommandBus commandBus) {
        this.commandBus = commandBus;
    }

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
            CreatePesArCommand createPesArCommand = objectMapper.readValue(message.getBody(), CreatePesArCommand.class);
            LOGGER.debug("Received a new PES AR command : {} {} {}", createPesArCommand.getId(),createPesArCommand.getFileContent(),createPesArCommand.getFileName());
            commandBus.dispatch(asCommandMessage(createPesArCommand));
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
        LOGGER.debug("Received a PES Sent message : {}", message.toString());
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            SendPesCommand sendPesCommand = objectMapper.readValue(message.getBody(), SendPesCommand.class);
            LOGGER.debug("Received a new PES Send command: {}", sendPesCommand.getId(),sendPesCommand.getPesId(),sendPesCommand.getDateSend());
            commandBus.dispatch(asCommandMessage(sendPesCommand));
        } catch (IOException e) {
            LOGGER.error("Unable to parse incoming PES Send", e);
        }
    }
}

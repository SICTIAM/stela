package fr.sictiam.stela.pescommand.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sictiam.stela.pescommand.command.CreatePesArCommand;
import fr.sictiam.stela.pescommand.command.SendPesCommand;
import org.axonframework.commandhandling.CommandBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static org.axonframework.commandhandling.GenericCommandMessage.asCommandMessage;

@Service
public class ReceiverService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReceiverService.class);
    private CommandBus commandBus;
    @Autowired
    public ReceiverService(CommandBus commandBus) {
        this.commandBus = commandBus;
    }
    /* @RabbitListener(bindings = @QueueBinding(
            value = @Queue,
            exchange = @Exchange(value = "${spring.application.exchange2}", type = ExchangeTypes.FANOUT,durable="true")))
    public void processIncomingPes(Message message) {
        LOGGER.debug("Received a message PES AR: {}", message.toString());
        LOGGER.debug("\twith body : {}", new String(message.getBody()));
    }
    */
    @RabbitListener(queues = "pesAr.queue")
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

    @RabbitListener(queues = "pesSend.queue")
    public void processPesSend(Message message) {
        LOGGER.debug("Received a message PES Send: {}", message.toString());
        LOGGER.debug("Received a body message PES Send: {}", message.getBody());
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            SendPesCommand pessendcommand = objectMapper.readValue(message.getBody(), SendPesCommand.class);
            LOGGER.debug("Received a new PES Send command: {}", pessendcommand.getId(),pessendcommand.getPesId(),pessendcommand.getDateSend());
            commandBus.dispatch(asCommandMessage(pessendcommand));

        } catch (IOException e) {
            LOGGER.error("Unable to parse incoming PES Send", e);
        }
    }
}

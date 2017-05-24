package fr.sictiam.stela.pes.dgfip.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sictiam.stela.pes.dgfip.model.Pes;
import fr.sictiam.stela.pes.dgfip.model.PesAr;
import fr.sictiam.stela.pes.dgfip.model.PesSend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class ReceiverService {

    @Value("${spring.application.exchange}")
    private String exchangeName;

    private final AmqpTemplate amqpTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(ReceiverService.class);

    public ReceiverService(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    String pesCreated = "pes.created";

    //@RabbitListener(queues = "pes.queue")
    @RabbitListener(bindings= @QueueBinding(
            value = @Queue(value = "pes.queue", durable = "true"),
            exchange = @Exchange(value = "pes.exchange", type = "topic", durable = "true"),
            key = "#pesCreated"))

    public void processIncomingPes(Message message) {
        LOGGER.debug("*************  message body {}",message);
        // TODO : Find a way to have automatic parsing from method arguments
        // Problem comes from AMQP message having contentType set to null
        Object receivedRoutingKey = message.getMessageProperties().getReceivedRoutingKey();
        LOGGER.debug("*************  message prpoertie routinekey***********");
        LOGGER.debug((String) receivedRoutingKey);
        if (receivedRoutingKey.equals(pesCreated) ) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                Pes pes = objectMapper.readValue(message.getBody(), Pes.class);
                LOGGER.debug("Received a new PES : {}", pes.toString());
            /* PesAr pesar = new PesAr(pes.getId());
            LOGGER.debug("Création Ar à envoyé: {}", pesar.toString()); */
                Date datej = new Date();
                SimpleDateFormat formatter = new SimpleDateFormat ("yyyy.MM.dd" );
                String datej2 = formatter.format(datej);
                PesSend pessend = new PesSend(pes.getId(),datej2);
                //amqpTemplate.convertAndSend("pesAr.exchange", "#", pesar.toString());
                amqpTemplate.convertAndSend("pesSend.exchange","#", pessend.toString());
            } catch (IOException e) {
                LOGGER.error("Unable to parse incoming PES", e);
            }
        }


    }
}

package fr.sictiam.stela.pes.dgfip.controller;

import fr.sictiam.stela.pes.dgfip.model.PesAr;
import fr.sictiam.stela.pes.dgfip.model.PesSend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.commandhandling.model.ConcurrencyException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(value = "/api/dgfip")
public class PesARController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PesARController.class);

    private CommandBus commandBus;
    private AmqpTemplate amqpTemplate;
    //@Autowired
     //public PesARController(CommandBus commandBus) {
     //   this.commandBus = commandBus;
    //}
    public PesARController(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }
    @RequestMapping( value ="/pesar",method = RequestMethod.POST)
    public void create(@RequestBody PesAr pesar, HttpServletResponse response) {
        LOGGER.debug("Got a PES AR flow to create {} ", pesar.toString());
        try {
            //envoi d'un message pour réception AR
            amqpTemplate.convertAndSend("pesAr.exchange","#", pesar.toString());
            response.setStatus(HttpServletResponse.SC_CREATED);
        } catch (AssertionError ae) {
            LOGGER.warn("Reception PES AR failed - empty param ? ''");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (CommandExecutionException cex) {
            LOGGER.warn("Add Command FAILED with message: {}", cex.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            if (null != cex.getCause()) {
                LOGGER.warn("Caused by: {} {}", cex.getCause().getClass().getName(), cex.getCause().getMessage());
                if (cex.getCause() instanceof ConcurrencyException) {
                    LOGGER.warn("A duplicate PES AR flow with the same ID [{}] already exists.", pesar.getFileContent());
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                }
            }
        }

    }

    @RequestMapping( value ="/pessend",method = RequestMethod.POST)
    public void pessend(@RequestBody PesSend pesSend, HttpServletResponse response) {
        LOGGER.debug("Got a PES Send {} {}", pesSend.getPesId(),pesSend.getDateSend());
        try {
            //envoi d'un message suite à envoi du message
            amqpTemplate.convertAndSend("pesSend.exchange","#", pesSend.toString());
            response.setStatus(HttpServletResponse.SC_CREATED);
        } catch (AssertionError ae) {
            LOGGER.warn("Reception PES Send failed - empty param ? ''");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (CommandExecutionException cex) {
            LOGGER.warn("Add pes send FAILED with message: {}", cex.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            if (null != cex.getCause()) {
                LOGGER.warn("Caused by: {} {}", cex.getCause().getClass().getName(), cex.getCause().getMessage());
                if (cex.getCause() instanceof ConcurrencyException) {
                    LOGGER.warn("A duplicate PES AR flow with the same ID [{}] already exists.", pesSend.getPesId());
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                }
            }
        }

    }
}

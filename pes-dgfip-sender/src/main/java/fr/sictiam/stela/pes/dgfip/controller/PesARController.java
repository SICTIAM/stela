package fr.sictiam.stela.pes.dgfip.controller;

import fr.sictiam.stela.pes.dgfip.model.PesAr;
import fr.sictiam.stela.pes.dgfip.model.PesSend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(value = "/api/dgfip")
public class PesARController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PesARController.class);

    private AmqpTemplate amqpTemplate;

    public PesARController(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    @RequestMapping( value ="/pesar",method = RequestMethod.POST)
    public void create(@RequestBody PesAr pesar, HttpServletResponse response) {
        LOGGER.debug("Got a PES AR flow to create {} ", pesar.toString());
        //envoi d'un message pour réception AR
        amqpTemplate.convertAndSend("pesAr.exchange","#", pesar.toString());
        response.setStatus(HttpServletResponse.SC_CREATED);
    }

    @RequestMapping( value ="/pessend",method = RequestMethod.POST)
    public void pessend(@RequestBody PesSend pesSend, HttpServletResponse response) {
        LOGGER.debug("Got a PES Send {} {}", pesSend.getPesId(),pesSend.getDateSend());
        //envoi d'un message suite à envoi du message
        amqpTemplate.convertAndSend("pesSend.exchange","#", pesSend.toString());
        response.setStatus(HttpServletResponse.SC_CREATED);
    }
}

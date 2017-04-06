package com.sictiam.flux_pes.controller;

import com.sictiam.flux_pes.command.CreatePesCommand;
import com.sictiam.flux_pes.model.Pes;
import com.sictiam.flux_pes.service.PesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/kafka")
public class KafkaController {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaController.class);

    @Autowired
    private PesService pesService;

    @RequestMapping(value = "/{message}", method = RequestMethod.GET)
    public void sendMessage(@PathVariable String message) {
        LOGGER.warn("Got a message to send !");
        CreatePesCommand pescom = new CreatePesCommand(message);
        pesService.create(pescom);
    }
}

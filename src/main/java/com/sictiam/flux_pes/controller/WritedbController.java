package com.sictiam.flux_pes.controller;

import com.sictiam.flux_pes.model.Pes;
import com.sictiam.flux_pes.model.PesObjet;
import com.sictiam.flux_pes.repository.PesRepo1;
import com.sictiam.flux_pes.repository.PesRepository;
import com.sictiam.flux_pes.service.PesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/writedb")
public class WritedbController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WritedbController.class);

    @Autowired
    private PesRepository pesRepository;
    @RequestMapping(value = "/{message}", method = RequestMethod.GET)
    public void accesDB() {
        LOGGER.warn("test acces db");
        Pes pes = new Pes ("5","titre");
        try {
            pesRepository.save(pes);
        }
        catch (Exception e ){
            LOGGER.error(e.getMessage());
        }
        for (Pes pesobj : pesRepository.findAll()) {
            LOGGER.info(pesobj.toString());
        }

    }
}

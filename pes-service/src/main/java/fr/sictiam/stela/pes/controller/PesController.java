package fr.sictiam.stela.pes.controller;

import fr.sictiam.stela.pes.model.Pes;
import fr.sictiam.stela.pes.service.PesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pes")
public class PesController {

    @Autowired
    private PesService pesService;

    private static final Logger LOGGER = LoggerFactory.getLogger(PesController.class);

    @GetMapping("")
    public List<Pes> getAll() {
        return pesService.getAll();
    }

    @PostMapping(value = "/new")
    public ResponseEntity<String> create(@RequestBody Pes pes) {
        LOGGER.debug("Got a PES flow to create {} {}", pes.getPesId(), pes.getTitle());
        pesService.create(pes);
        return new ResponseEntity<>(HttpStatus.CREATED);
}
}

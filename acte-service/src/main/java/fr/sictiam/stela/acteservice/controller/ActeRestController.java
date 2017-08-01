package fr.sictiam.stela.acteservice.controller;

import java.util.List;

import fr.sictiam.stela.acteservice.service.ActeNotSentException;
import fr.sictiam.stela.acteservice.service.ActeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

import fr.sictiam.stela.acteservice.model.Acte;

@RestController
@RequestMapping("/api/acte")
public class ActeRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActeRestController.class);

    private ActeService acteService;

    @Autowired
    public ActeRestController(ActeService acteService){
        this.acteService = acteService;
    }

    @GetMapping
    public ResponseEntity<List<Acte>> readActes() {
        List<Acte> actes = acteService.getAll();
        return new ResponseEntity<List<Acte>>(actes, HttpStatus.OK);
    }

    @GetMapping("/id/{uuid}")
    public ResponseEntity<Acte> getById(@PathVariable String uuid) {
        Acte acte = acteService.getByUuid(uuid);
        return new ResponseEntity<Acte>(acte, HttpStatus.OK);
    }


    @PostMapping
    ResponseEntity<String> create(@RequestParam("acte") String acteJson, @RequestParam("file") MultipartFile file, @RequestParam("annexes") MultipartFile... annexes) {
        
        ObjectMapper mapper = new ObjectMapper();
        try {
            Acte acte = mapper.readValue(acteJson, Acte.class);
            
            LOGGER.info("Received acte : {}", acte.getTitle());
            LOGGER.info("Received main file : {}", file.getOriginalFilename());
            LOGGER.info("Received {} annexes : ", annexes.length);

            Acte result = acteService.createAndSend(acte, file, annexes);
            return new ResponseEntity<>(result.getUuid(), HttpStatus.OK);

        } catch (IOException e) {
            LOGGER.error("IOException: Could not convert JSON to Acte: {}", e);
            return new ResponseEntity<>("notifications.acte.sent.error.non_extractable_acte", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (ActeNotSentException ns){
            LOGGER.error("ActeNotSentException: {}", ns);
            return new ResponseEntity<>("notifications.acte.sent.error.acte_not_sent", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

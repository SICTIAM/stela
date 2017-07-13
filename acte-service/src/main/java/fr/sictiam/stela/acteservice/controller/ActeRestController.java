package fr.sictiam.stela.acteservice.controller;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.dao.ActeRepository;

@RestController
@RequestMapping("/acte")
public class ActeRestController {

    private ActeRepository repository;

    @Autowired
    ActeRestController(ActeRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<Acte>> readActes() {
        List<Acte> actes = this.repository.findAll();
        return new ResponseEntity<List<Acte>>(actes, HttpStatus.OK);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Acte> getById(@PathVariable Long id) {
        Acte acte = this.repository.findByUuid(id).orElseThrow(ActeNotFoundException::new);
        return new ResponseEntity<Acte>(acte, HttpStatus.OK);
    }

    @GetMapping("/numero/{numero}")
    public ResponseEntity<Acte> getByNumero(@PathVariable String numero) {
        Acte acte = this.repository.findByNumero(numero).orElseThrow(ActeNotFoundException::new);
        return new ResponseEntity<Acte>(acte, HttpStatus.OK);
    }

    @PostMapping
    ResponseEntity<?> create(@RequestBody Acte acte) {
        Acte result = this.repository.save(acte);
        URI location = ServletUriComponentsBuilder
                        .fromCurrentRequest()
                        .path("/id/{id}")
                        .buildAndExpand(result.getUuid())
                        .toUri();

        return ResponseEntity.created(location).build();
    }
}
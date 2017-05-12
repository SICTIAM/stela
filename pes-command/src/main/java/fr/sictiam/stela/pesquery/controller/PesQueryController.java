package fr.sictiam.stela.pesquery.controller;

import fr.sictiam.stela.pesquery.dao.PesRepository;
import fr.sictiam.stela.pesquery.model.PesEntry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/pes-query")
public class PesQueryController {

    private final PesRepository pesRepository;

    public PesQueryController(PesRepository pesRepository) {
        this.pesRepository = pesRepository;
    }

    @GetMapping("")
    public List<PesEntry> getAll() {
        return pesRepository.findAll();
    }
}

package fr.sictiam.stela.pesservice.controller;

import java.net.URLConnection;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.sictiam.stela.pesservice.model.PesAller;
import fr.sictiam.stela.pesservice.model.StatusType;
import fr.sictiam.stela.pesservice.service.LocalAuthorityService;
import fr.sictiam.stela.pesservice.service.PesAllerService;

@RestController
@RequestMapping("/api/pes")
public class PesRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PesRestController.class);

    private final PesAllerService pesService;
    private final LocalAuthorityService localAuthorityService;

    @Autowired
    public PesRestController(PesAllerService pesService, LocalAuthorityService localAuthorityService) {
        this.pesService = pesService;
        this.localAuthorityService = localAuthorityService;
    }

    @GetMapping
    public ResponseEntity<List<PesAller>> getAll(@RequestParam(value = "number", required = false) String number,
            @RequestParam(value = "objet", required = false) String objet,
            @RequestParam(value = "decisionFrom", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate decisionFrom,
            @RequestParam(value = "decisionTo", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate decisionTo,
            @RequestParam(value = "status", required = false) StatusType status) {
        List<PesAller> pesList = pesService.getAllWithQuery(number, objet, decisionFrom, decisionTo, status);
        return new ResponseEntity<>(pesList, HttpStatus.OK);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<PesAller> getByUuid(
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @PathVariable String uuid) {
        PesAller pes = pesService.getByUuid(uuid);

        return new ResponseEntity<>(pes, HttpStatus.OK);
    }
}

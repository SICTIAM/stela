package fr.sictiam.stela.pesservice.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import fr.sictiam.stela.pesservice.model.PesAller;
import fr.sictiam.stela.pesservice.model.StatusType;
import fr.sictiam.stela.pesservice.service.LocalAuthorityService;
import fr.sictiam.stela.pesservice.service.PesAllerService;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping
    public ResponseEntity<String> create(@RequestAttribute("STELA-Current-Profile-UUID") String currentProfileUuid,
                                         @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
                                         @RequestParam("pesAller") String pesAllerJson, @RequestParam("file") MultipartFile file) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            PesAller pesAller = mapper.readValue(pesAllerJson, PesAller.class);
            PesAller result = pesService.create(currentProfileUuid, currentLocalAuthUuid, pesAller, file);
            return new ResponseEntity<>(result.getUuid(), HttpStatus.CREATED);
        } catch (IOException e) {
            LOGGER.error("IOException: Could not convert JSON to PesAller: {}", e);
            return new ResponseEntity<>("notifications.pes.sent.error.non_extractable_pes", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

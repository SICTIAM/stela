package fr.sictiam.stela.acteservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.model.ActeNature;
import fr.sictiam.stela.acteservice.model.AttachmentType;
import fr.sictiam.stela.acteservice.model.LocalAuthority;
import fr.sictiam.stela.acteservice.model.MaterialCode;
import fr.sictiam.stela.acteservice.service.ActeService;
import fr.sictiam.stela.acteservice.service.LocalAuthorityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/editeur/api/acte")
public class EditeurRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActeRestController.class);

    private final ActeService acteService;
    private final LocalAuthorityService localAuthorityService;

    public EditeurRestController(ActeService acteService, LocalAuthorityService localAuthorityService) {
        this.acteService = acteService;
        this.localAuthorityService = localAuthorityService;
    }

    @PostMapping
    ResponseEntity<String> create(
            @RequestParam("number") String number,
            @RequestParam("objet") String objet,
            @RequestParam("nature") ActeNature nature,
            @RequestParam("code") String code,
            @RequestParam("decision") LocalDate decision,
            @RequestParam("file") MultipartFile file,
            @RequestParam("fileType") String fileType,
            @RequestParam(value = "annexes", required = false) MultipartFile[] annexes,
            @RequestParam(value = "annexeTypes", required = false) String[] annexeTypes,
            @RequestParam(value = "public", required = false) Boolean isPublic,
            @RequestParam(value = "publicWebsite", required = false) Boolean isPublicWebsite,
            @RequestParam(value = "groupUuid", required = false) String groupUuid,
            @RequestParam(value = "email", required = false) String email)
            throws IOException {

        // TODO: Retrieve localAuthority from auth
        LocalAuthority localAuthority = new LocalAuthority();
        Acte acte = acteService.create(number, objet, nature, code, decision, isPublic, isPublicWebsite, groupUuid,
                file, fileType, annexes, annexeTypes, email, localAuthority);
        return new ResponseEntity<>(acte.getUuid(), HttpStatus.OK);
    }

    @GetMapping("/codes-matieres")
    ResponseEntity<List<MaterialCode>> getCodesMatieres() {
        // TODO: Retrieve localAuthority from auth
        LocalAuthority localAuthority = new LocalAuthority();
        return new ResponseEntity<>(localAuthorityService.getCodesMatieres(localAuthority.getUuid()), HttpStatus.OK);
    }

    @GetMapping("/attachment-types/{acteNature}/{materialCode}")
    public ResponseEntity<Set<AttachmentType>> getAttachmentTypesForNature(@PathVariable ActeNature acteNature,
            @PathVariable String materialCode) {
        // TODO: Retrieve localAuthority from auth
        LocalAuthority localAuthority = new LocalAuthority();
        return new ResponseEntity<>(
                localAuthorityService.getAttachmentTypeAvailable(acteNature, localAuthority.getUuid(), materialCode),
                HttpStatus.OK);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<Acte> getByUuid(@PathVariable String uuid) {
        // TODO: Remove localAuthority infos
        Acte acte = acteService.getByUuid(uuid);
        return new ResponseEntity<>(acte, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Acte>> getActeList(
            @RequestParam(value = "limit", required = false, defaultValue = "25") Integer limit,
            @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
            @RequestParam(value = "column", required = false, defaultValue = "creation") String column,
            @RequestParam(value = "direction", required = false, defaultValue = "DESC") String direction) {
        // TODO: Retrieve localAuthority from auth
        LocalAuthority localAuthority = new LocalAuthority();
        List<Acte> acteList = acteService.getAllWithQueryNoSearch(limit, offset, column, direction, localAuthority.getUuid());
        return new ResponseEntity<>(acteList, HttpStatus.OK);
    }

    @GetMapping("/group")
    public ResponseEntity<JsonNode> getGroups() throws IOException {
        // TODO: Retrieve localAuthority from auth
        LocalAuthority localAuthority = new LocalAuthority();
        JsonNode node = acteService.getGroups(localAuthority.getUuid());
        return new ResponseEntity<>(node, HttpStatus.OK);
    }

    @PostMapping("/{uuid}/cancel")
    public ResponseEntity cancelActe(@PathVariable String uuid) {
        acteService.cancel(uuid);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/{uuid}/courrier-simple")
    public ResponseEntity sendCourrierSimple(@PathVariable String uuid, @RequestParam("file") MultipartFile file)
            throws IOException {
        acteService.sendReponseCourrierSimple(uuid, file);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/{uuid}/pieces-complementaires")
    public ResponseEntity sendPiecesComplementaires(@PathVariable String uuid, @RequestParam("files") MultipartFile[] files)
            throws IOException {
        acteService.sendReponsePiecesComplementaires(uuid, "reponse", files);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/{uuid}/pieces-complementaires/reject")
    public ResponseEntity refusPiecesComplementaires(@PathVariable String uuid, @RequestParam("file") MultipartFile file)
            throws IOException {
        acteService.sendReponsePiecesComplementaires(uuid, "reject", new MultipartFile[]{file});
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/{uuid}/lettre-observation")
    public ResponseEntity sendReponseObservations(@PathVariable String uuid, @RequestParam("file") MultipartFile file)
            throws IOException {
        acteService.sendReponseLettreObservation(uuid, "reponse", file);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/{uuid}/lettre-observation/reject")
    public ResponseEntity sendRefusObservations(@PathVariable String uuid, @RequestParam("file") MultipartFile file)
            throws IOException {
        acteService.sendReponseLettreObservation(uuid, "reject", file);
        return new ResponseEntity(HttpStatus.OK);
    }
}

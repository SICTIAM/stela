package fr.sictiam.stela.acteservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.model.ActeNature;
import fr.sictiam.stela.acteservice.model.ActeParams;
import fr.sictiam.stela.acteservice.model.LocalAuthority;
import fr.sictiam.stela.acteservice.model.ui.CustomValidationUI;
import fr.sictiam.stela.acteservice.model.util.Certificate;
import fr.sictiam.stela.acteservice.service.ActeService;
import fr.sictiam.stela.acteservice.service.ExternalRestService;
import fr.sictiam.stela.acteservice.service.LocalAuthorityService;
import fr.sictiam.stela.acteservice.service.ValidationService;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/editeur/api/acte")
public class EditeurRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EditeurRestController.class);

    private final ActeService acteService;
    private final LocalAuthorityService localAuthorityService;
    private final ValidationService validationService;
    private final ExternalRestService externalRestService;

    public EditeurRestController(ActeService acteService, LocalAuthorityService localAuthorityService, ValidationService validationService, ExternalRestService externalRestService) {
        this.acteService = acteService;
        this.localAuthorityService = localAuthorityService;
        this.validationService = validationService;
        this.externalRestService = externalRestService;
    }

    @PostMapping()
    ResponseEntity<?> create(HttpServletRequest request,
            @RequestAttribute(value = "STELA-Certificate", required = true) Certificate certificate,
            @RequestParam("number") String number,
            @RequestParam("objet") String objet,
            @RequestParam("nature") ActeNature nature,
            @RequestParam("code") String code,
            @RequestParam("decision") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate decision,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "fileType", required = false) String fileType,
            @RequestParam(value = "annexes", required = false) MultipartFile[] annexes,
            @RequestParam(value = "annexeTypes", required = false) String[] annexeTypes,
            @RequestParam(value = "public", required = false, defaultValue = "false") Boolean isPublic,
            @RequestParam(value = "publicWebsite", required = false, defaultValue = "false") Boolean isPublicWebsite,
            @RequestParam(value = "groupUuid", required = false) String groupUuid,
            @RequestParam(value = "email", required = false) String email)
            throws IOException {

        LocalAuthority localAuthority;
        if (certificate == null ||
                (localAuthority = externalRestService.getLocalAuthorityByCertificate(certificate)) == null)
            return new ResponseEntity<>("Authentication failed", HttpStatus.UNAUTHORIZED);

        ActeParams acteParams = new ActeParams(
                number,
                objet,
                nature,
                code,
                decision,
                file,
                fileType,
                Arrays.asList(annexes != null ? annexes : new MultipartFile[]{}),
                Arrays.asList(annexeTypes != null ? annexeTypes : new String[]{}),
                isPublic,
                isPublicWebsite,
                groupUuid,
                email,
                localAuthority
        );

        List<ObjectError> errors = validationService.validateActeParams(acteParams);
        if (!errors.isEmpty())
            return new ResponseEntity<>(new CustomValidationUI(errors, "has failed"), HttpStatus.BAD_REQUEST);

        // Check number existence here to send another return code
        if (acteService.numberExist(acteParams.getNumber(), acteParams.getLocalAuthority().getUuid())) {
            return new ResponseEntity<>(
                    new CustomValidationUI(
                            new FieldError("acte", "number", "Number " + acteParams.getNumber() + " already exists"), "conflict", HttpStatus.CONFLICT), HttpStatus.CONFLICT);
        }

        Acte acte = acteService.create(number, objet, nature, code, decision, isPublic, isPublicWebsite, groupUuid,
                file, fileType, annexes, annexeTypes, email, localAuthority);
        return new ResponseEntity<>(acte.getUuid(), HttpStatus.OK);
    }

    @GetMapping("/codes-matieres")
    ResponseEntity<?> getCodesMatieres(@RequestAttribute(value = "STELA-Certificate", required = true) Certificate certificate) {
        LocalAuthority localAuthority;
        if (certificate == null ||
                (localAuthority = externalRestService.getLocalAuthorityByCertificate(certificate)) == null)
            return new ResponseEntity<>("Authentication failed", HttpStatus.UNAUTHORIZED);

        return new ResponseEntity<>(localAuthorityService.getCodesMatieres(localAuthority.getUuid()), HttpStatus.OK);
    }

    @GetMapping("/attachment-types/{acteNature}/{materialCode}")
    public ResponseEntity<?> getAttachmentTypesForNature(
            @RequestAttribute(value = "STELA-Certificate", required = true) Certificate certificate,
            @PathVariable ActeNature acteNature,
            @PathVariable String materialCode) {
        LocalAuthority localAuthority;
        if (certificate == null ||
                (localAuthority = externalRestService.getLocalAuthorityByCertificate(certificate)) == null)
            return new ResponseEntity<>("Authentication failed", HttpStatus.UNAUTHORIZED);

        return new ResponseEntity<>(
                localAuthorityService.getAttachmentTypeAvailable(acteNature, localAuthority.getUuid(), materialCode),
                HttpStatus.OK);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<?> getByUuid(
            @RequestAttribute(value = "STELA-Certificate", required = true) Certificate certificate,
            @PathVariable String uuid) {
        LocalAuthority localAuthority;
        if (certificate == null ||
                (localAuthority = externalRestService.getLocalAuthorityByCertificate(certificate)) == null)
            return new ResponseEntity<>("Authentication failed", HttpStatus.UNAUTHORIZED);

        Acte acte = acteService.getByUuid(uuid);
        return new ResponseEntity<>(acte, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<?> getActeList(
            @RequestAttribute(value = "STELA-Certificate", required = true) Certificate certificate,
            @RequestParam(value = "limit", required = false, defaultValue = "25") Integer limit,
            @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
            @RequestParam(value = "column", required = false, defaultValue = "creation") String column,
            @RequestParam(value = "direction", required = false, defaultValue = "DESC") String direction) {
        LocalAuthority localAuthority;
        if (certificate == null ||
                (localAuthority = externalRestService.getLocalAuthorityByCertificate(certificate)) == null)
            return new ResponseEntity<>("Authentication failed", HttpStatus.UNAUTHORIZED);

        List<Acte> acteList = acteService.getAllFull(limit, offset, column, direction, localAuthority.getUuid());
        return new ResponseEntity<>(acteList, HttpStatus.OK);
    }

    @GetMapping("/group")
    public ResponseEntity<?> getGroups(@RequestAttribute(value = "STELA-Certificate", required = true) Certificate certificate) throws IOException {
        LocalAuthority localAuthority;
        if (certificate == null ||
                (localAuthority = externalRestService.getLocalAuthorityByCertificate(certificate)) == null)
            return new ResponseEntity<>("Authentication failed", HttpStatus.UNAUTHORIZED);

        JsonNode node = acteService.getGroups(localAuthority.getUuid());
        return new ResponseEntity<>(node, HttpStatus.OK);
    }

    @PostMapping("/{uuid}/cancel")
    public ResponseEntity cancelActe(@RequestAttribute(value = "STELA-Certificate", required = true) Certificate certificate,

            @PathVariable String uuid) {
        LocalAuthority localAuthority;
        if (certificate == null ||
                (localAuthority = externalRestService.getLocalAuthorityByCertificate(certificate)) == null)
            return new ResponseEntity<>("Authentication failed", HttpStatus.UNAUTHORIZED);

        acteService.cancel(uuid);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/{uuid}/courrier-simple")
    public ResponseEntity sendCourrierSimple(
            @RequestAttribute(value = "STELA-Certificate", required = true) Certificate certificate,
            @PathVariable String uuid, @RequestParam("file") MultipartFile file)
            throws IOException {
        LocalAuthority localAuthority;
        if (certificate == null ||
                (localAuthority = externalRestService.getLocalAuthorityByCertificate(certificate)) == null)
            return new ResponseEntity<>("Authentication failed", HttpStatus.UNAUTHORIZED);

        acteService.sendReponseCourrierSimple(uuid, file);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/{uuid}/pieces-complementaires")
    public ResponseEntity sendPiecesComplementaires(
            @RequestAttribute(value = "STELA-Certificate", required = true) Certificate certificate,
            @PathVariable String uuid,
            @RequestParam("files") MultipartFile[] files)
            throws IOException {
        LocalAuthority localAuthority;
        if (certificate == null ||
                (localAuthority = externalRestService.getLocalAuthorityByCertificate(certificate)) == null)
            return new ResponseEntity<>("Authentication failed", HttpStatus.UNAUTHORIZED);

        acteService.sendReponsePiecesComplementaires(uuid, "reponse", files);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/{uuid}/pieces-complementaires/reject")
    public ResponseEntity refusPiecesComplementaires(
            @RequestAttribute(value = "STELA-Certificate", required = true) Certificate certificate,
            @PathVariable String uuid,
            @RequestParam("file") MultipartFile file)
            throws IOException {
        LocalAuthority localAuthority;
        if (certificate == null ||
                (localAuthority = externalRestService.getLocalAuthorityByCertificate(certificate)) == null)
            return new ResponseEntity<>("Authentication failed", HttpStatus.UNAUTHORIZED);

        acteService.sendReponsePiecesComplementaires(uuid, "reject", new MultipartFile[]{file});
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/{uuid}/lettre-observation")
    public ResponseEntity sendReponseObservations(
            @RequestAttribute(value = "STELA-Certificate", required = true) Certificate certificate,
            @PathVariable String uuid,
            @RequestParam("file") MultipartFile file)
            throws IOException {
        LocalAuthority localAuthority;
        if (certificate == null ||
                (localAuthority = externalRestService.getLocalAuthorityByCertificate(certificate)) == null)
            return new ResponseEntity<>("Authentication failed", HttpStatus.UNAUTHORIZED);

        acteService.sendReponseLettreObservation(uuid, "reponse", file);
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/{uuid}/lettre-observation/reject")
    public ResponseEntity sendRefusObservations(
            @RequestAttribute(value = "STELA-Certificate", required = true) Certificate certificate,
            @PathVariable String uuid,
            @RequestParam("file") MultipartFile file)
            throws IOException {
        LocalAuthority localAuthority;
        if (certificate == null ||
                (localAuthority = externalRestService.getLocalAuthorityByCertificate(certificate)) == null)
            return new ResponseEntity<>("Authentication failed", HttpStatus.UNAUTHORIZED);

        acteService.sendReponseLettreObservation(uuid, "reject", file);
        return new ResponseEntity(HttpStatus.OK);
    }
}

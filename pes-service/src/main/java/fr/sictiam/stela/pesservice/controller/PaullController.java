package fr.sictiam.stela.pesservice.controller;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import fr.sictiam.stela.pesservice.model.LocalAuthority;
import fr.sictiam.stela.pesservice.model.PesAller;
import fr.sictiam.stela.pesservice.model.PesHistory;
import fr.sictiam.stela.pesservice.model.StatusType;
import fr.sictiam.stela.pesservice.model.sesile.Classeur;
import fr.sictiam.stela.pesservice.model.ui.GenericAccount;
import fr.sictiam.stela.pesservice.service.ExternalRestService;
import fr.sictiam.stela.pesservice.service.LocalAuthorityService;
import fr.sictiam.stela.pesservice.service.PesAllerService;
import fr.sictiam.stela.pesservice.service.PesRetourService;
import fr.sictiam.stela.pesservice.service.SesileService;
import fr.sictiam.stela.pesservice.service.StorageService;
import fr.sictiam.stela.pesservice.service.exceptions.PesCreationException;
import fr.sictiam.stela.pesservice.validation.ValidationUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.xml.security.utils.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rest/externalws/{siren}/fr/classic/webservhelios/services/api/rest.php/")
public class PaullController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaullController.class);

    private final SesileService sesileService;
    private final LocalAuthorityService localAuthorityService;
    private final PesAllerService pesAllerService;
    private final ExternalRestService externalRestService;
    private final PesRetourService pesRetourService;
    private final StorageService storageService;

    public PaullController(SesileService sesileService, LocalAuthorityService localAuthorityService,
            PesAllerService pesAllerService, ExternalRestService externalRestService,
            PesRetourService pesRetourService, StorageService storageService) {
        this.sesileService = sesileService;
        this.localAuthorityService = localAuthorityService;
        this.pesAllerService = pesAllerService;
        this.externalRestService = externalRestService;
        this.pesRetourService = pesRetourService;
        this.storageService = storageService;
    }

    @JsonPropertyOrder({ "status", "status_message", "data" })
    class PaullResponse {

        String status;
        String status_message;
        Object data;

        public PaullResponse() {

        }

        public PaullResponse(String status, String status_message, Object data) {
            this.status = status;
            this.status_message = status_message;
            this.data = data;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getStatus_message() {
            return status_message;
        }

        public void setStatus_message(String status_message) {
            this.status_message = status_message;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }

    }

    public PaullResponse generatePaullResponse(HttpStatus httpStatus, Object datas) {
        return new PaullResponse(httpStatus.value() + "", httpStatus.getReasonPhrase(), datas);
    }

    public GenericAccount emailAuth(String email, String password) {

        GenericAccount genericAccount = null;
        try {

            genericAccount = externalRestService.authWithEmailPassword(email, password);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        return genericAccount;
    }

    @PostMapping("/depotpes")
    public ResponseEntity<?> DepotPES(@PathVariable String siren, MultipartHttpServletRequest request,
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "comment", required = false) String comment,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "desc", required = false) String desc,
            @RequestParam(name = "validation", required = false) String validation,
            @RequestParam(name = "service", required = false) String service,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "PESPJ", required = false, defaultValue = "0") int PESPJ,
            @RequestParam(name = "SSLSerial", required = false) String SSLSerial,
            @RequestParam(name = "SSLVendor", required = false) String SSLVendor,
            @RequestHeader("userid") String userid, @RequestHeader("password") String password) {

        Iterator<String> itrator = request.getFileNames();
        MultipartFile multiFile = request.getFile(itrator.next());

        LOGGER.debug("FILENAME : " + multiFile.getName());
        GenericAccount genericAccount = emailAuth(userid, password);
        siren = StringUtils.removeStart(siren, "sys");
        HttpStatus status = HttpStatus.OK;
        Map<String, Object> data = new HashMap<>();

        if (genericAccount == null || !localAuthorityService.localAuthorityGranted(genericAccount, siren)) {
            status = HttpStatus.FORBIDDEN;
            return new ResponseEntity<Object>(generatePaullResponse(status, data), status);
        }

        try {
            if (pesAllerService.checkVirus(multiFile.getBytes())) {
                return new ResponseEntity<>("notifications.pes.sent.virus", HttpStatus.BAD_REQUEST);
            }
            PesAller pesAller = new PesAller();
            LOGGER.debug("Received a PES with title : {}", title);
            String decodedTitle = new String(title.getBytes("Windows-1252"));
            LOGGER.debug("Decoded title to {}", decodedTitle);
            try {
                String decodedTitleWithW1252 = new String(title.getBytes(Charset.forName("Windows-1252")), Charset.forName("UTF-8"));
                LOGGER.debug("Decoded title with Windows-1252 to {}", decodedTitleWithW1252);
                String decodedTitleWithISO8859 = new String(title.getBytes(Charset.forName("ISO-8859-1")), Charset.forName("UTF-8"));
                LOGGER.debug("Decoded title with ISO-8859-1 to {}", decodedTitleWithISO8859);
            } catch (Exception e) {
                LOGGER.error("Error testing PES title decoding", e);
            }
            String decodedComment = new String(comment.getBytes("Windows-1252"));
            pesAller.setObjet(decodedTitle);
            pesAller.setComment(decodedComment);
            // Probably source of bug : PES from Ciril are not send to Sesile
            //pesAller.setPj(PESPJ == 0 ? false : true);
            if (StringUtils.isNotBlank(service)) {
                pesAller.setServiceOrganisationNumber(Integer.parseInt(service));
            }
            if (StringUtils.isNotBlank(validation)) {
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate deadline = LocalDate.parse(validation, dateFormatter);
                pesAller.setValidationLimit(deadline);

            }
            List<ObjectError> errors = ValidationUtil.validatePes(pesAller);
            if (!errors.isEmpty()) {
                status = HttpStatus.BAD_REQUEST;
                return new ResponseEntity<Object>(generatePaullResponse(status, data), status);
            }

            Optional<LocalAuthority> localAuthority = localAuthorityService.getBySiren(siren);
            if (localAuthority.isPresent()) {
                String currentProfileUuid;
                String currentLocalAuthUuid = localAuthority.get().getUuid();

                if (StringUtils.isEmpty(email)) {
                    currentProfileUuid = localAuthority.get().getGenericProfileUuid();
                } else {
                    JsonNode jsonNode = externalRestService.getProfileByLocalAuthoritySirenAndEmail(siren, email);
                    currentProfileUuid = jsonNode.get("uuid").asText();
                }
                PesAller result = pesAllerService.create(currentProfileUuid, currentLocalAuthUuid, pesAller, multiFile);
                data.put("idFlux", result.getUuid());
                return new ResponseEntity<Object>(generatePaullResponse(status, data), status);
            }

        } catch (IOException e) {
            throw new PesCreationException();
        }

        return new ResponseEntity<Object>(generatePaullResponse(status, data), status);
    }

    @GetMapping("/infospes/{idFlux}")
    public ResponseEntity<?> infoPes(@PathVariable String siren, @PathVariable String idFlux,
            @RequestHeader("userid") String userid, @RequestHeader("password") String password) throws IOException {

        Map<String, Object> data = new HashMap<>();
        HttpStatus status = HttpStatus.OK;
        siren = StringUtils.removeStart(siren, "sys");
        GenericAccount genericAccount = emailAuth(userid, password);
        if (genericAccount == null || !localAuthorityService.localAuthorityGranted(genericAccount, siren)) {
            status = HttpStatus.FORBIDDEN;
            return new ResponseEntity<Object>(generatePaullResponse(status, data), status);
        }

        PesAller pesAller = pesAllerService.getByUuid(idFlux);
        Optional<LocalAuthority> localAuthority = localAuthorityService.getBySiren(siren);

        // PES PJ are not sent to signature
        if(!pesAller.isPj()) {
            ResponseEntity<Classeur> classeur = sesileService.checkClasseurStatus(localAuthority.get(),
                    pesAller.getSesileClasseurId());

            if (classeur.getStatusCode().isError()) {
                return new ResponseEntity<Object>(generatePaullResponse(classeur.getStatusCode(), data), classeur.getStatusCode());
            }

            data.put("Name", classeur.getBody().getNom());
            data.put("EtatClasseur", classeur.getBody().getStatus().ordinal());
        }

        JsonNode node = externalRestService.getProfile(pesAller.getProfileUuid());

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        data.put("Title", pesAller.getObjet());
        data.put("Username", node.get("email").asText());
        data.put("NomDocument", pesAller.getFileName());
        data.put("dateDepot", dateFormatter.format(pesAller.getCreation()));
        data.put("service", pesAller.getServiceOrganisationNumber());

        List<PesHistory> fileHistories = pesAllerService.getPesHistoryByTypes(idFlux,
                Arrays.asList(StatusType.ACK_RECEIVED, StatusType.NACK_RECEIVED));

        Optional<PesHistory> peshistory = fileHistories.stream().findFirst();

        if (peshistory.isPresent()) {
            if (peshistory.get().getStatus().equals(StatusType.ACK_RECEIVED)) {
                data.put("dateAR", dateFormatter.format(peshistory.get().getDate()));
            } else if (peshistory.get().getStatus().equals(StatusType.NACK_RECEIVED)) {
                data.put("dateAnomalie", dateFormatter.format(peshistory.get().getDate()));
                data.put("motifAnomalie", peshistory.get().getErrors().get(0).errorText());
            }
        }

        return new ResponseEntity<Object>(generatePaullResponse(status, data), status);

    }

    @GetMapping("/docpes/{idFlux}")
    public ResponseEntity<?> docpes(@PathVariable String siren, @PathVariable String idFlux,
            @RequestHeader("userid") String userid, @RequestHeader("password") String password) throws IOException {

        Map<String, Object> data = new HashMap<>();
        HttpStatus status = HttpStatus.OK;
        siren = StringUtils.removeStart(siren, "sys");
        GenericAccount genericAccount = emailAuth(userid, password);
        if (genericAccount == null || !localAuthorityService.localAuthorityGranted(genericAccount, siren)) {
            status = HttpStatus.FORBIDDEN;
            return new ResponseEntity<Object>(generatePaullResponse(status, data), status);
        }

        PesAller pesAller = pesAllerService.getByUuid(idFlux);

        data.put("NomDocument", pesAller.getAttachment().getFilename());

        data.put("Contenu", Base64.encode(storageService.getAttachmentContent(pesAller.getAttachment())));
        return new ResponseEntity<Object>(generatePaullResponse(status, data), status);

    }

    @GetMapping("/getPESRetour/{idCommune}")
    public ResponseEntity<?> getPESRetour(@PathVariable String siren, @PathVariable String idCommune,
            @RequestParam(name = "majauto", required = false, defaultValue = "0") int majauto,
            @RequestHeader("userid") String userid, @RequestHeader("password") String password) {
        return getPESRetours(siren, majauto, userid, password);
    }

    @GetMapping("/getPESRetour")
    public ResponseEntity<?> getPESRetourForSiren(@PathVariable String siren,
            @RequestParam(name = "majauto", required = false, defaultValue = "0") int majauto,
            @RequestHeader("userid") String userid, @RequestHeader("password") String password) {
        return getPESRetours(siren, majauto, userid, password);
    }

    private ResponseEntity<?> getPESRetours(String siren, int majauto, String userid, String password) {

        HttpStatus status = HttpStatus.OK;
        siren = StringUtils.removeStart(siren, "sys");
        GenericAccount genericAccount = emailAuth(userid, password);
        if (genericAccount == null || !localAuthorityService.localAuthorityGranted(genericAccount, siren)) {
            status = HttpStatus.FORBIDDEN;
            return new ResponseEntity<Object>(generatePaullResponse(status, null), status);
        }

        List<Map<String, String>> outputList = pesRetourService.getUncollectedLocalAuthorityPesRetours(siren);
        if (majauto == 1) {
            outputList.forEach(pesRetour -> pesRetourService.collect(pesRetour.get("filename")));
        }
        return new ResponseEntity<Object>(generatePaullResponse(status, outputList), status);
    }

    @GetMapping("/servicespes/{email}")
    public ResponseEntity<?> servicespes(@PathVariable String siren, @PathVariable String email,
            @RequestHeader("userid") String userid, @RequestHeader("password") String password) {

        HttpStatus status = HttpStatus.OK;
        siren = StringUtils.removeStart(siren, "sys");
        GenericAccount genericAccount = emailAuth(userid, password);
        if (genericAccount == null || !localAuthorityService.localAuthorityGranted(genericAccount, siren)) {
            status = HttpStatus.FORBIDDEN;
            return new ResponseEntity<Object>(generatePaullResponse(status, null), status);
        }

        Optional<LocalAuthority> localAuthority = localAuthorityService.getBySiren(siren);
        if (localAuthority.isPresent()) {
            List<Map<String, Object>> validationCircuits = sesileService
                    .getHeliosServiceOrganisations(localAuthority.get(), email).stream().map(circuit -> {
                        Map<String, Object> circuitInfo = new HashMap<>();
                        circuitInfo.put("id", circuit.getId());
                        circuitInfo.put("nom", circuit.getNom());
                        return circuitInfo;
                    }).collect(Collectors.toList());
            return new ResponseEntity<Object>(generatePaullResponse(status, validationCircuits), status);

        } else {
            status = HttpStatus.BAD_REQUEST;
            return new ResponseEntity<Object>(generatePaullResponse(status, null), status);

        }

    }

    @GetMapping("/sendACKPESRetour/{fileName}")
    public ResponseEntity<?> getPESRetour(@PathVariable String siren, @PathVariable String fileName,
            @RequestHeader("userid") String userid, @RequestHeader("password") String password) {

        HttpStatus status = HttpStatus.OK;
        siren = StringUtils.removeStart(siren, "sys");

        GenericAccount genericAccount = emailAuth(userid, password);
        if (genericAccount == null || !localAuthorityService.localAuthorityGranted(genericAccount, siren)) {
            status = HttpStatus.FORBIDDEN;
            return new ResponseEntity<Object>(generatePaullResponse(status, null), status);
        }

        pesRetourService.collect(fileName);
        return new ResponseEntity<Object>(generatePaullResponse(status, null), status);

    }

}

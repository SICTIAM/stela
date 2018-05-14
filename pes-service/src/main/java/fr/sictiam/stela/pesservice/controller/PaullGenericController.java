package fr.sictiam.stela.pesservice.controller;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import fr.sictiam.stela.pesservice.model.GenericDocument;
import fr.sictiam.stela.pesservice.model.LocalAuthority;
import fr.sictiam.stela.pesservice.model.sesile.Classeur;
import fr.sictiam.stela.pesservice.model.sesile.ClasseurRequest;
import fr.sictiam.stela.pesservice.model.sesile.Document;
import fr.sictiam.stela.pesservice.model.sesile.ServiceOrganisation;
import fr.sictiam.stela.pesservice.model.ui.GenericAccount;
import fr.sictiam.stela.pesservice.service.ExternalRestService;
import fr.sictiam.stela.pesservice.service.LocalAuthorityService;
import fr.sictiam.stela.pesservice.service.SesileService;
import org.apache.commons.lang3.StringUtils;
import org.apache.xml.security.utils.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/rest/externalws/{siren}/fr/classic/webservgeneriques/services/api/rest.php/")
public class PaullGenericController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaullGenericController.class);

    private final SesileService sesileService;
    private final LocalAuthorityService localAuthorityService;
    private final ExternalRestService externalRestService;

    public PaullGenericController(SesileService sesileService, LocalAuthorityService localAuthorityService,
            ExternalRestService externalRestService) {
        this.sesileService = sesileService;
        this.localAuthorityService = localAuthorityService;
        this.externalRestService = externalRestService;
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
        return generatePaullResponse(httpStatus, datas, null);
    }

    public PaullResponse generatePaullResponse(HttpStatus httpStatus, Object datas, String customMessage) {
        return new PaullResponse(httpStatus.value() + "",
                StringUtils.isNoneBlank(customMessage) ? customMessage : httpStatus.getReasonPhrase(), datas);
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

    @PostMapping("/depot")
    public ResponseEntity<?> depotDoc(@PathVariable String siren, MultipartHttpServletRequest request,
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "comment", required = false) String comment,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "desc", required = false) String desc,
            @RequestParam(name = "validation", required = false) String validation,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "type ", required = false) Integer type,
            @RequestParam(name = "service", required = false) String service, @RequestHeader("userid") String userid,
            @RequestHeader("password") String password) {

        Iterator<String> itrator = request.getFileNames();
        MultipartFile multiFile = request.getFile(itrator.next());

        GenericAccount genericAccount = emailAuth(userid, password);
        siren = StringUtils.removeStart(siren, "sys");
        HttpStatus status = HttpStatus.OK;
        Map<String, Object> data = new HashMap<>();

        if (genericAccount == null) {
            status = HttpStatus.FORBIDDEN;
            return new ResponseEntity<Object>(generatePaullResponse(status, data), status);
        }
        Integer serviceOrganisation = Integer.parseInt(service);
        Optional<LocalAuthority> localAuthority = localAuthorityService.getBySiren(siren);
        if (localAuthority.isPresent()) {
            ResponseEntity<Classeur> classeur = sesileService.postClasseur(localAuthority.get(),
                    new ClasseurRequest(name, desc, validation, type, serviceOrganisation, 3, email));

            try {
                ResponseEntity<Document> documentResonse = sesileService.addFileToclasseur(localAuthority.get(),
                        multiFile.getBytes(), multiFile.getOriginalFilename(), classeur.getBody().getId());
                if (documentResonse.getStatusCode().is2xxSuccessful()) {
                    data.put("idFlux", classeur.getBody().getId());
                    sesileService.saveGenericDocument(
                            new GenericDocument(classeur.getBody().getId(), documentResonse.getBody().getId(),
                                    serviceOrganisation, email, LocalDateTime.now(), localAuthority.get()));
                }
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }

        }

        return new ResponseEntity<Object>(generatePaullResponse(status, data), status);
    }

    @GetMapping("/infosdoc/{idFlux}")
    public ResponseEntity<?> infosDoc(@PathVariable String siren, @PathVariable Integer idFlux,
            @RequestHeader("userid") String userid, @RequestHeader("password") String password) throws IOException {

        Map<String, Object> data = new HashMap<>();
        HttpStatus status = HttpStatus.OK;
        siren = StringUtils.removeStart(siren, "sys");
        GenericAccount genericAccount = emailAuth(userid, password);
        String message = "OK";
        if (genericAccount == null) {
            status = HttpStatus.FORBIDDEN;
            return new ResponseEntity<Object>(generatePaullResponse(status, data), status);
        }

        Optional<LocalAuthority> localAuthority = localAuthorityService.getBySiren(siren);

        if (localAuthority.isPresent()) {
            ResponseEntity<Classeur> classeurResponse = sesileService.checkClasseurStatus(localAuthority.get(), idFlux);
            Optional<GenericDocument> genericDocument = sesileService.getGenericDocument(idFlux);
            if (classeurResponse.hasBody() && genericDocument.isPresent()) {
                Classeur classeur = classeurResponse.getBody();

                if (!classeur.getDocuments().isEmpty()) {
                    Document document = classeur.getDocuments().get(0);
                    data.put("Title", classeur.getNom());

                    data.put("Name", classeur.getNom());
                    data.put("Username", genericDocument.get().getDepositEmail());
                    data.put("NomDocument", document.getName());
                    data.put("dateDepot", classeur.getCreation());
                    data.put("datevalide", classeur.getValidation());

                    data.put("service", classeur.getCircuit());
                    data.put("EtatClasseur", classeur.getStatus().ordinal());
                } else {
                    status = HttpStatus.BAD_REQUEST;
                    message = "No document in Classeur";
                }

            } else {
                status = HttpStatus.BAD_REQUEST;
                message = "Classeur was not found";
            }

        } else {
            status = HttpStatus.BAD_REQUEST;
            message = "LocalAuthority was not found";
        }
        return new ResponseEntity<Object>(generatePaullResponse(status, data, message), status);
    }

    @GetMapping("/doc/{idFlux}")
    public ResponseEntity<?> docpes(@PathVariable String siren, @PathVariable Integer idFlux,
            @RequestHeader("userid") String userid, @RequestHeader("password") String password) throws IOException {

        Map<String, Object> data = new HashMap<>();
        HttpStatus status = HttpStatus.OK;
        siren = StringUtils.removeStart(siren, "sys");
        GenericAccount genericAccount = emailAuth(userid, password);
        if (genericAccount == null) {
            status = HttpStatus.FORBIDDEN;
            return new ResponseEntity<Object>(generatePaullResponse(status, data), status);
        }
        String message = "OK";

        Optional<LocalAuthority> localAuthority = localAuthorityService.getBySiren(siren);

        if (localAuthority.isPresent()) {
            ResponseEntity<Classeur> classeurResponse = sesileService.checkClasseurStatus(localAuthority.get(), idFlux);

            if (classeurResponse.hasBody()) {
                Classeur classeur = classeurResponse.getBody();

                if (!classeur.getDocuments().isEmpty()) {
                    Document document = classeur.getDocuments().get(0);
                    data.put("NomDocument", document.getName());

                    data.put("Contenu",
                            Base64.encode(sesileService.getDocumentBody(localAuthority.get(), document.getId())));
                    data.put("datevalide", classeur.getValidation());

                } else {
                    status = HttpStatus.BAD_REQUEST;
                    message = "No document in Classeur";
                }

            } else {
                status = HttpStatus.BAD_REQUEST;
                message = "Classeur was not found";
            }

        } else {
            status = HttpStatus.BAD_REQUEST;
            message = "LocalAuthority was not found";
        }

        return new ResponseEntity<Object>(generatePaullResponse(status, data, message), status);
    }

    @GetMapping("/servicesorganisationnels/{email}")
    public ResponseEntity<?> services(@PathVariable String siren, @PathVariable String email,
            @RequestHeader("userid") String userid, @RequestHeader("password") String password) {

        HttpStatus status = HttpStatus.OK;
        siren = StringUtils.removeStart(siren, "sys");
        GenericAccount genericAccount = emailAuth(userid, password);
        if (genericAccount == null) {
            status = HttpStatus.FORBIDDEN;
            return new ResponseEntity<Object>(generatePaullResponse(status, null), status);
        }

        Optional<LocalAuthority> localAuthority = localAuthorityService.getBySiren(siren);
        if (localAuthority.isPresent()) {
            List<ServiceOrganisation> validationCircuits = sesileService
                    .getServiceGenericOrganisations(localAuthority.get(), email);
            return new ResponseEntity<Object>(generatePaullResponse(status, validationCircuits), status);

        } else {
            status = HttpStatus.BAD_REQUEST;
            return new ResponseEntity<Object>(generatePaullResponse(status, null), status);
        }
    }
}

package fr.sictiam.stela.pesservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import fr.sictiam.stela.pesservice.model.GenericDocument;
import fr.sictiam.stela.pesservice.model.LocalAuthority;
import fr.sictiam.stela.pesservice.model.sesile.Classeur;
import fr.sictiam.stela.pesservice.model.sesile.ClasseurRequest;
import fr.sictiam.stela.pesservice.model.sesile.Document;
import fr.sictiam.stela.pesservice.model.sesile.ServiceOrganisation;
import fr.sictiam.stela.pesservice.model.util.PaullResponse;
import fr.sictiam.stela.pesservice.service.ExternalRestService;
import fr.sictiam.stela.pesservice.service.LocalAuthorityService;
import fr.sictiam.stela.pesservice.service.PaullService;
import fr.sictiam.stela.pesservice.service.SesileService;
import org.apache.commons.lang3.StringUtils;
import org.apache.xml.security.utils.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static fr.sictiam.stela.pesservice.service.util.JsonExtractorUtils.*;

@RestController
@RequestMapping("/rest/externalws/{siren}/fr/classic/webservgeneriques/services/api/rest.php/")
public class PaullGenericController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaullGenericController.class);

    private final SesileService sesileService;
    private final LocalAuthorityService localAuthorityService;
    private final PaullService paullService;
    private final ExternalRestService externalRestService;

    public PaullGenericController(SesileService sesileService, LocalAuthorityService localAuthorityService,
                                  PaullService paullService, ExternalRestService externalRestService) {
        this.sesileService = sesileService;
        this.localAuthorityService = localAuthorityService;
        this.paullService = paullService;
        this.externalRestService = externalRestService;
    }

    private PaullResponse generatePaullResponse(HttpStatus httpStatus, Object datas) {
        return generatePaullResponse(httpStatus, datas, null);
    }

    private PaullResponse generatePaullResponse(HttpStatus httpStatus, Object datas, String customMessage) {
        return new PaullResponse(httpStatus.value() + "",
                StringUtils.isNoneBlank(customMessage) ? customMessage : httpStatus.getReasonPhrase(), datas);
    }

    @PostMapping("/depot")
    public ResponseEntity<?> depotDocument(@PathVariable String siren, MultipartHttpServletRequest request,
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "comment", required = false) String comment,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "desc", required = false) String desc,
            @RequestParam(name = "validation", required = false) String validation,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "type", required = false) Integer type,
            @RequestParam(name = "service", required = false) String service,
            @RequestHeader("userid") String userid,
            @RequestHeader("password") String password) {

        Iterator<String> itrator = request.getFileNames();
        MultipartFile multiFile = request.getFile(itrator.next());
        Map<String, Object> data = new HashMap<>();

        if (paullService.emailAuth(userid, password) == null) return this.authGenericAccountForbidden(data);

        HttpStatus status = HttpStatus.OK;

        if (service == null)
            return new ResponseEntity<Object>(generatePaullResponse(HttpStatus.BAD_REQUEST, data), HttpStatus.BAD_REQUEST);

        if (type == null)
            return new ResponseEntity<Object>(generatePaullResponse(HttpStatus.BAD_REQUEST, data), HttpStatus.BAD_REQUEST);

        Optional<LocalAuthority> optLocalAuthority = localAuthorityService.getBySiren(StringUtils.removeStart(siren, "sys"));
        if (!optLocalAuthority.isPresent()) {
            LOGGER.error("No local authority found for SIREN {}", StringUtils.removeStart(siren, "sys"));
            return new ResponseEntity<Object>(generatePaullResponse(HttpStatus.NOT_FOUND, data), HttpStatus.NOT_FOUND);
        }

        LocalAuthority localAuthority = optLocalAuthority.get();

        if (email != null) {
            email = email.trim();
        } else {
            String genericProfileUuid = localAuthority.getGenericProfileUuid();
            try {
                JsonNode profile = externalRestService.getProfile(genericProfileUuid);
                email = extractEmailFromProfile(profile);
            } catch (IOException e) {
                LOGGER.error("Error while retrieving generic profile {}", genericProfileUuid, e);
                return new ResponseEntity<Object>(generatePaullResponse(HttpStatus.INTERNAL_SERVER_ERROR, data),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        Integer serviceOrganisation = Integer.parseInt(service);

        validation = sesileService.getSesileValidationDate(validation, localAuthority.getGenericProfileUuid());

        try {
            Classeur classeur = sesileService.postClasseur(localAuthority,
                    new ClasseurRequest(name, desc, validation, type, serviceOrganisation, 3, email), null);
            Document document = sesileService.addFileToclasseur(localAuthority,
                    multiFile.getBytes(), multiFile.getOriginalFilename(), classeur.getId());
            data.put("idFlux", classeur.getId());
            sesileService.saveGenericDocument(
                    new GenericDocument(classeur.getId(), document.getId(),
                            serviceOrganisation, email, LocalDateTime.now(), localAuthority));
        } catch (RestClientResponseException e) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            LOGGER.error("Error from Sesile : {} | Body : {}", e.getMessage(), e.getResponseBodyAsString());
        } catch (IOException e) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            LOGGER.error("Failed to read bytes from given file : {}", e.getMessage());
        }

        return new ResponseEntity<Object>(generatePaullResponse(status, data), status);
    }

    @GetMapping("/infosdoc/{idFlux}")
    public ResponseEntity<?> infosDocument(@PathVariable String siren, @PathVariable Integer idFlux,
            @RequestHeader("userid") String userid, @RequestHeader("password") String password) throws IOException {

        Map<String, Object> data = new HashMap<>();

        if (paullService.emailAuth(userid, password) == null) return this.authGenericAccountForbidden(data);

        Optional<LocalAuthority> localAuthority = localAuthorityService.getBySiren(StringUtils.removeStart(siren, "sys"));

        if (!localAuthority.isPresent()) return this.notFoundLocalAuthority(data);

        ResponseEntity<Classeur> classeurResponse = sesileService.checkClasseurStatus(localAuthority.get(), idFlux);
        Optional<GenericDocument> genericDocument = sesileService.getGenericDocument(idFlux);

        if ((classeurResponse.getStatusCode().isError() && !classeurResponse.hasBody()) || !genericDocument.isPresent())
            return this.notFoundClasseur(data);

        Classeur classeur = classeurResponse.getBody();

        if (classeur == null)
            return this.errorWhileSearchingClasseur(data);

        if (classeur.getDocuments().isEmpty())
            return this.noDocumentInClasseur(data);

        Document document = classeur.getDocuments().get(0);
        data.put("Title", classeur.getNom());
        data.put("Name", classeur.getNom());
        data.put("Username", genericDocument.get().getDepositEmail());
        data.put("NomDocument", document.getName());
        data.put("dateDepot", classeur.getCreation());
        data.put("datevalide", classeur.getValidation());
        data.put("service", classeur.getCircuit());
        data.put("EtatClasseur", classeur.getStatus().ordinal());

        return new ResponseEntity<Object>(generatePaullResponse(HttpStatus.OK, data, "OK"), HttpStatus.OK);
    }

    @GetMapping("/doc/{idFlux}")
    public ResponseEntity<?> getDocument(@PathVariable String siren, @PathVariable Integer idFlux,
            @RequestHeader("userid") String userid, @RequestHeader("password") String password) throws IOException {

        Map<String, Object> data = new HashMap<>();

        if (paullService.emailAuth(userid, password) == null) return this.authGenericAccountForbidden(data);

        Optional<LocalAuthority> localAuthority = localAuthorityService.getBySiren(StringUtils.removeStart(siren, "sys"));

        if (!localAuthority.isPresent()) return this.notFoundLocalAuthority(data);

        ResponseEntity<Classeur> classeurResponse = sesileService.checkClasseurStatus(localAuthority.get(), idFlux);

        if (classeurResponse.getStatusCode().isError() && !classeurResponse.hasBody())
            return this.notFoundClasseur(data);

        Classeur classeur = classeurResponse.getBody();

        if (classeur == null) return this.errorWhileSearchingClasseur(data);

        if (classeur.getDocuments().isEmpty()) return this.noDocumentInClasseur(data);

        Document document = classeur.getDocuments().get(0);
        data.put("NomDocument", document.getName());
        data.put("Contenu", Base64.encode(sesileService.getDocumentBody(localAuthority.get(), document.getId())));
        data.put("datevalide", classeur.getValidation());

        return new ResponseEntity<Object>(generatePaullResponse(HttpStatus.OK, data, "OK"), HttpStatus.OK);
    }

    @GetMapping("/servicesorganisationnels/{email}")
    public ResponseEntity<?> getServices(@PathVariable String siren, @PathVariable String email,
            @RequestHeader("userid") String userid, @RequestHeader("password") String password) {
        if (paullService.emailAuth(userid, password) == null) return this.authGenericAccountForbidden(null);

        Optional<LocalAuthority> localAuthority = localAuthorityService.getBySiren(StringUtils.removeStart(siren, "sys"));
        if (localAuthority.isPresent()) {
            List<ServiceOrganisation> validationCircuits = sesileService
                    .getServiceGenericOrganisations(localAuthority.get(), email);
            return new ResponseEntity<Object>(generatePaullResponse(HttpStatus.OK, validationCircuits), HttpStatus.OK);

        } else {
            return new ResponseEntity<Object>(generatePaullResponse(HttpStatus.BAD_REQUEST, null), HttpStatus.BAD_REQUEST);
        }
    }

    private ResponseEntity<Object> notFoundLocalAuthority(Map data) {
        return new ResponseEntity<>(
                generatePaullResponse(HttpStatus.BAD_REQUEST, data, "LocalAuthority was not found"), HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<Object> notFoundClasseur(Map data) {
        return new ResponseEntity<>(
                generatePaullResponse(HttpStatus.BAD_REQUEST, data, "Classeur was not found"), HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<Object> errorWhileSearchingClasseur(Map data) {
        return new ResponseEntity<>(
                generatePaullResponse(HttpStatus.BAD_REQUEST, data, "Error occurred while searching Classeur"), HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<Object> noDocumentInClasseur(Map data) {
        return new ResponseEntity<>(
                generatePaullResponse(HttpStatus.BAD_REQUEST, data, "No document in Classeur"), HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<Object> authGenericAccountForbidden(Map data) {
        return new ResponseEntity<>(generatePaullResponse(HttpStatus.FORBIDDEN, data), HttpStatus.FORBIDDEN);
    }
}

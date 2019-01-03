package fr.sictiam.stela.pesservice.soap.endpoints;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import fr.sictiam.stela.pesservice.soap.model.paull.*;
import fr.sictiam.stela.pesservice.validation.ValidationUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.apache.commons.lang3.StringUtils;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Endpoint
public class PaullEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaullEndpoint.class);

    private static final String NAMESPACE_URI = "http://www.processmaker.com";

    @Value("${application.jwt.secret}")
    String SECRET;

    private final PesAllerService pesAllerService;
    private final PesRetourService pesRetourService;
    private final LocalAuthorityService localAuthorityService;
    private final ExternalRestService externalRestService;
    private final SesileService sesileService;
    private final StorageService storageService;

    public PaullEndpoint(PesAllerService pesAllerService, LocalAuthorityService localAuthorityService,
            SoapReturnGenerator soapReturnGenerator, ExternalRestService externalRestService,
            PesRetourService pesRetourService, SesileService sesileService, StorageService storageService) {
        this.pesAllerService = pesAllerService;
        this.localAuthorityService = localAuthorityService;
        this.externalRestService = externalRestService;
        this.pesRetourService = pesRetourService;
        this.sesileService = sesileService;
        this.storageService = storageService;
    }

    PaullSoapToken getToken(String sessionID) {

        if (sessionID != null) {

            try {
                String token = externalRestService.getPaullConnection(sessionID).get("token").asText();
                Claims tokenClaim = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();
                if (tokenClaim.getExpiration().before(new Date())) {
                    return null;
                }
                String tokenParsed = tokenClaim.getSubject();

                ObjectMapper objectMapper = new ObjectMapper();
                PaullSoapToken node;
                node = objectMapper.readValue(tokenParsed, PaullSoapToken.class);

                return node;
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
                return null;
            }

        }
        return null;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "depotPESAllerRequest")
    public @ResponsePayload
    DepotPESAllerResponse depotPESAller(@RequestPayload DepotPESAllerRequest depotPesAller)
            throws IOException, Base64DecodingException {
        PaullSoapToken paullSoapToken = getToken(depotPesAller.getSessionId());

        DepotPESAllerResponse depotPESAllerResponse = new DepotPESAllerResponse();

        DepotPESAllerStruct depotPESAllerStruct = new DepotPESAllerStruct();

        String returnMessage = "UNKNOW_ERROR";
        String status = "NOK";
        if (paullSoapToken == null) {
            returnMessage = "SESSION_INVALID_OR_EXPIRED";
            LOGGER.error(returnMessage);

        } else {
            GenericAccount genericAccount = externalRestService.getGenericAccount(paullSoapToken.getAccountUuid());

            if (!localAuthorityService.localAuthorityGranted(genericAccount, paullSoapToken.getSiren())) {
                returnMessage = "LOCALAUTHORITY_NOT_GRANTED";

            } else {
                DepotPESAllerStruct1 depotPESAllerStruct1 = depotPesAller.getInfosPESAller().get(0);
                LOGGER.debug(depotPESAllerStruct1.toString());
                byte[] file = Base64.decode(depotPesAller.getFichier().get(0).getBase64().getBytes("UTF-8"));
                String name = StringUtils.stripAccents(depotPesAller.getFichier().get(0).getFilename());
                if (pesAllerService.checkVirus(file)) {
                    returnMessage = "VIRUS_FOUND";
                } else {
                    PesAller pesAller = new PesAller();
                    pesAller.setObjet(depotPESAllerStruct1.getTitle());
                    pesAller.setComment(depotPESAllerStruct1.getComment());
                    List<ObjectError> errors = ValidationUtil.validatePes(pesAller);
                    if (!errors.isEmpty()) {
                        returnMessage = "INVALID_DATAS";
                    }

                    pesAller.setCreation(LocalDateTime.now());

                    if (StringUtils.isNotBlank(depotPESAllerStruct1.getGroupid())) {
                        pesAller.setServiceOrganisationNumber(Integer.parseInt(depotPESAllerStruct1.getGroupid()));
                    }
                    if (depotPESAllerStruct1.getPESPJ() != 1
                            && StringUtils.isNotBlank(depotPESAllerStruct1.getValidation())) {
                        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        LocalDate deadline = LocalDate.parse(depotPESAllerStruct1.getValidation(), dateFormatter);
                        pesAller.setValidationLimit(deadline);
                    }
                    pesAller.setPj(depotPESAllerStruct1.getPESPJ() == 1);

                    if (pesAllerService.getByFileName(pesAller.getFileName()).isPresent()) {
                        returnMessage = "DUPLICATE_FILE";
                    } else {
                        Optional<LocalAuthority> localAuthority = localAuthorityService
                                .getBySiren(paullSoapToken.getSiren());
                        if (localAuthority.isPresent()) {
                            String currentProfileUuid;
                            String currentLocalAuthUuid = localAuthority.get().getUuid();

                            if (StringUtils.isEmpty(depotPESAllerStruct1.getEmail())) {
                                currentProfileUuid = localAuthority.get().getGenericProfileUuid();
                            } else {
                                JsonNode jsonNode = externalRestService.getProfileByLocalAuthoritySirenAndEmail(
                                        paullSoapToken.getSiren(), depotPESAllerStruct1.getEmail());
                                currentProfileUuid = jsonNode.get("uuid").asText();
                            }
                            PesAller result = pesAllerService.create(currentProfileUuid, currentLocalAuthUuid,
                                    pesAller, name, file);
                            status = "OK";
                            returnMessage = "SUCCES";
                            depotPESAllerStruct.setIdPesAller(result.getUuid());
                        }
                    }

                }

            }

        }

        depotPESAllerResponse.setStatusCode(status);
        depotPESAllerStruct.setMessage(returnMessage);
        depotPESAllerResponse.getRetour().add(depotPESAllerStruct);

        return depotPESAllerResponse;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getDetailsPESAllerRequest")
    public @ResponsePayload
    GetDetailsPESAllerResponse getDetailsPESAller(
            @RequestPayload GetDetailsPESAllerRequest getDetailsPESAllerRequest) throws IOException {

        PaullSoapToken paullSoapToken = getToken(getDetailsPESAllerRequest.getSessionId());

        GetDetailsPESAllerResponse detailsPESAllerResponse = new GetDetailsPESAllerResponse();

        GetDetailsPESAllerStruct detailsPESAllerStruct = new GetDetailsPESAllerStruct();

        String returnMessage = "UNKNOW_ERROR";
        String status = "NOK";
        if (paullSoapToken == null) {
            returnMessage = "SESSION_INVALID_OR_EXPIRED";
        } else {
            PesAller pesAller = pesAllerService.getByUuid(getDetailsPESAllerRequest.getIdPesAller());
            Optional<LocalAuthority> localAuthority = localAuthorityService.getBySiren(paullSoapToken.getSiren());

            GenericAccount genericAccount = externalRestService.getGenericAccount(paullSoapToken.getAccountUuid());

            if (localAuthorityService.localAuthorityGranted(genericAccount, paullSoapToken.getSiren())) {

                ResponseEntity<Classeur> classeur = sesileService.checkClasseurStatus(localAuthority.get(),
                        pesAller.getSesileClasseurId());

                if (classeur.getStatusCode().isError()) {
                    status = "NOK";
                    returnMessage = classeur.getStatusCode() + ": " + classeur.getStatusCode().getReasonPhrase();
                } else {
                    JsonNode node = externalRestService.getProfile(pesAller.getProfileUuid());
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                    detailsPESAllerStruct.setPESPJ(pesAller.isPj() ? "1" : "0");
                    detailsPESAllerStruct.setObjet(pesAller.getObjet());
                    detailsPESAllerStruct.setNomClasseur(classeur.getBody().getNom());
                    detailsPESAllerStruct.setUserName(node.get("email").asText());
                    detailsPESAllerStruct.setNomDocument(pesAller.getFileName());
                    detailsPESAllerStruct.setDateDepot(dateFormatter.format(pesAller.getCreation()));

                    List<PesHistory> fileHistories = pesAllerService.getPesHistoryByTypes(
                            getDetailsPESAllerRequest.getIdPesAller(),
                            Arrays.asList(StatusType.ACK_RECEIVED, StatusType.NACK_RECEIVED));

                    Optional<PesHistory> peshistory = fileHistories.stream().findFirst();

                    if (peshistory.isPresent()) {
                        if (peshistory.get().getStatus().equals(StatusType.ACK_RECEIVED)) {
                            detailsPESAllerStruct.setDateAR(dateFormatter.format(peshistory.get().getDate()));
                        } else if (peshistory.get().getStatus().equals(StatusType.NACK_RECEIVED)) {
                            detailsPESAllerStruct.setDateAnomalie(dateFormatter.format(peshistory.get().getDate()));
                            detailsPESAllerStruct.setMotifAnomalie(peshistory.get().getErrors().get(0).errorText());
                        }
                    }

                    detailsPESAllerStruct.setEtatclasseur(classeur.getBody().getStatus().ordinal() + "");
                    returnMessage = "SUCCESS";
                    status = "OK";
                }

            } else {
                returnMessage = "LOCALAUTHORITY_NOT_GRANTED";
            }
        }
        detailsPESAllerStruct.setMessage(returnMessage);
        detailsPESAllerResponse.getRetour().add(detailsPESAllerStruct);
        detailsPESAllerResponse.setStatusCode(status);
        return detailsPESAllerResponse;

    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getPESAllerRequest")
    public @ResponsePayload
    GetPESAllerResponse getPESAller(@RequestPayload GetPESAllerRequest getPESAllerRequest)
            throws IOException {

        PaullSoapToken paullSoapToken = getToken(getPESAllerRequest.getSessionId());

        GetPESAllerResponse pesAllerResponse = new GetPESAllerResponse();

        GetPESAllerStruct pesAllerStruct = new GetPESAllerStruct();

        String returnMessage = "UNKNOW_ERROR";
        String status = "NOK";
        if (paullSoapToken == null) {
            returnMessage = "SESSION_INVALID_OR_EXPIRED";
        } else {
            GenericAccount genericAccount = externalRestService.getGenericAccount(paullSoapToken.getAccountUuid());

            if (localAuthorityService.localAuthorityGranted(genericAccount, paullSoapToken.getSiren())) {
                PesAller pesAller = pesAllerService.getByUuid(getPESAllerRequest.getIdPesAller());

                pesAllerStruct.setFilename(pesAller.getAttachment().getFilename());
                pesAllerStruct.setBase64(Base64.encode(storageService.getAttachmentContent(pesAller.getAttachment())));

                returnMessage = "SUCCESS";
                status = "OK";
            } else {
                returnMessage = "LOCALAUTHORITY_NOT_GRANTED";
            }
        }

        pesAllerStruct.setMessage(returnMessage);
        pesAllerResponse.getRetour().add(pesAllerStruct);
        pesAllerResponse.setStatusCode(status);
        return pesAllerResponse;

    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getPESRetourRequest")
    public @ResponsePayload
    GetPESRetourResponse getPESRetour(@RequestPayload GetPESRetourRequest getPESRetourRequest)
            throws IOException {

        PaullSoapToken paullSoapToken = getToken(getPESRetourRequest.getSessionId());

        GetPESRetourResponse pesAllerResponse = new GetPESRetourResponse();

        GetPESRetourStruct pesRetourStruct = new GetPESRetourStruct();

        String returnMessage = "UNKNOW_ERROR";
        String status = "NOK";
        if (paullSoapToken == null) {
            returnMessage = "SESSION_INVALID_OR_EXPIRED";
        } else {
            GenericAccount genericAccount = externalRestService.getGenericAccount(paullSoapToken.getAccountUuid());

            if (localAuthorityService.localAuthorityGranted(genericAccount, paullSoapToken.getSiren())) {
                List<GetTabPESRetourStruct> outputList = pesRetourService
                        .getPaullUncollectedLocalAuthorityPesRetours(paullSoapToken.getSiren());

                if (getPESRetourRequest.getMajauto() == 1) {
                    outputList.forEach(pesRetour -> pesRetourService.collect(pesRetour.getFilename()));
                }
                pesRetourStruct.getTabpesretour().addAll(outputList);
                returnMessage = "SUCCESS";
                status = "OK";
            } else {
                returnMessage = "LOCALAUTHORITY_NOT_GRANTED";
            }
        }

        pesRetourStruct.setMessage(returnMessage);
        pesAllerResponse.getRetour().add(pesRetourStruct);
        pesAllerResponse.setStatusCode(status);
        return pesAllerResponse;

    }

}
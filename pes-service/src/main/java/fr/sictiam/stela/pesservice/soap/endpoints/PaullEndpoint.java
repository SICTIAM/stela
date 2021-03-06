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
import fr.sictiam.stela.pesservice.service.exceptions.PesCreationException;
import fr.sictiam.stela.pesservice.service.util.JsonExtractorUtils;
import fr.sictiam.stela.pesservice.soap.model.paull.*;
import fr.sictiam.stela.pesservice.validation.ValidationUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.vavr.control.Either;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.validation.ObjectError;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Endpoint
public class PaullEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaullEndpoint.class);

    private static final String NAMESPACE_URI = "http://www.processmaker.com";

    private DateTimeFormatter dateFormatterWithMinutes = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Value("${application.jwt.secret}")
    String SECRET;

    private final PesAllerService pesAllerService;
    private final PesRetourService pesRetourService;
    private final LocalAuthorityService localAuthorityService;
    private final ExternalRestService externalRestService;
    private final SesileService sesileService;

    public PaullEndpoint(PesAllerService pesAllerService, LocalAuthorityService localAuthorityService,
            ExternalRestService externalRestService, PesRetourService pesRetourService, SesileService sesileService) {
        this.pesAllerService = pesAllerService;
        this.localAuthorityService = localAuthorityService;
        this.externalRestService = externalRestService;
        this.pesRetourService = pesRetourService;
        this.sesileService = sesileService;
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
                return objectMapper.readValue(tokenParsed, PaullSoapToken.class);
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
        LOGGER.info("Received a PES Aller : {}", depotPesAller);
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
                LOGGER.error(returnMessage);
            } else {
                DepotPESAllerStruct1 depotPESAllerStruct1 = depotPesAller.getInfosPESAller().get(0);
                byte[] file = Base64.decode(depotPesAller.getFichier().get(0).getBase64().getBytes("UTF-8"));
                String name = StringUtils.stripAccents(depotPesAller.getFichier().get(0).getFilename());
                if (pesAllerService.checkVirus(file)) {
                    returnMessage = "VIRUS_FOUND";
                    LOGGER.error(returnMessage);
                } else {
                    try {
                        PesAller pesAller = new PesAller();
                        pesAller.setObjet(depotPESAllerStruct1.getTitle());
                        pesAller.setComment(depotPESAllerStruct1.getComment());
                        List<ObjectError> errors = ValidationUtil.validatePes(pesAller);
                        if (!errors.isEmpty()) {
                            returnMessage = "INVALID_DATAS";
                            LOGGER.error("Received PES is invalid : {}", errors);
                            // TODO : don't go further is data is invalid
                            // TODO : is there really a point validating on the two above fields ??
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
                            try {
                                PesAller result = pesAllerService.create(currentProfileUuid, currentLocalAuthUuid,
                                        pesAller, name, file);
                                status = "OK";
                                returnMessage = "SUCCES";
                                depotPESAllerStruct.setIdPesAller(result.getUuid());
                                LOGGER.debug(String.format("Created PES (PJ : %b) with id : %s", result.isPj(), result.getUuid()));
                            } catch (PesCreationException pce) {
                                LOGGER.error("Error while creating PES", pce);
                                returnMessage = String.format("CREATION_ERROR : %s", pce.getMessage());
                            }
                        } else {
                            LOGGER.error("Unable to find local authority from SIREN found in token : {}", paullSoapToken.getSiren());
                            returnMessage = String.format("UNKOWN_SIREN : %s", paullSoapToken.getSiren());
                        }
                    } catch (Exception e) {
                        LOGGER.error("Error while dealing with received PES", e);
                        returnMessage = String.format("CREATION_ERROR : %s", e.getMessage());
                    }
                }
            }
        }

        depotPESAllerResponse.setStatusCode(status);
        depotPESAllerStruct.setMessage(returnMessage);
        depotPESAllerResponse.getRetour().add(depotPESAllerStruct);

        LOGGER.info("Returning : {}", depotPESAllerResponse);

        return depotPESAllerResponse;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getDetailsPESAllerRequest")
    public @ResponsePayload
    GetDetailsPESAllerResponse getDetailsPESAller(
            @RequestPayload GetDetailsPESAllerRequest getDetailsPESAllerRequest) throws IOException {

        PaullSoapToken paullSoapToken = getToken(getDetailsPESAllerRequest.getSessionId());
        if (paullSoapToken == null)
            return getDetailsPESAllerResponseError("SESSION_INVALID_OR_EXPIRED");

        GenericAccount genericAccount = externalRestService.getGenericAccount(paullSoapToken.getAccountUuid());
        if (!localAuthorityService.localAuthorityGranted(genericAccount, paullSoapToken.getSiren()))
            return getDetailsPESAllerResponseError("LOCALAUTHORITY_NOT_GRANTED");

        GetDetailsPESAllerResponse detailsPESAllerResponse = new GetDetailsPESAllerResponse();
        GetDetailsPESAllerStruct detailsPESAllerStruct = new GetDetailsPESAllerStruct();

        PesAller pesAller = pesAllerService.getByUuid(getDetailsPESAllerRequest.getIdPesAller());
        Optional<LocalAuthority> localAuthority = localAuthorityService.getBySiren(paullSoapToken.getSiren());

        // PES PJ are not sent to signature, don't bother checking something impossible
        if (!pesAller.isPj() && !pesAllerService.isAPesOrmc(pesAller) && pesAller.getSesileClasseurId() != null) {
            Either<HttpStatus, Classeur> sesileResponse = sesileService.getClasseur(localAuthority.get(),
                    pesAller);
            if (sesileResponse.isLeft()) {
                return getDetailsPESAllerResponseError(sesileResponse.getLeft() + ": "
                        + sesileResponse.getLeft().getReasonPhrase());
            }

            Classeur classeur = sesileResponse.get();
            detailsPESAllerStruct.setNomClasseur(classeur.getNom());
            detailsPESAllerStruct.setEtatclasseur(classeur.getStatus().ordinal() + "");
            if (classeur.getCircuit() != null)
                detailsPESAllerStruct.setCircuitClasseur(classeur.getCircuit());
            else
                detailsPESAllerStruct.setCircuitClasseur("");
            classeur.getActions().forEach(action -> {
                GetDetailsPESAllerStruct1 xmlAction = new GetDetailsPESAllerStruct1();
                xmlAction.setDateAction(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(action.getDate()));
                xmlAction.setLibelleAction(action.getAction());
                xmlAction.setNomActeur(action.getUsername());
                detailsPESAllerStruct.getActionsClasseur().add(xmlAction);
            });
        } else {
            detailsPESAllerStruct.setNomClasseur("");
            detailsPESAllerStruct.setEtatclasseur("");
            detailsPESAllerStruct.setCircuitClasseur("");
        }

        JsonNode node = externalRestService.getProfile(pesAller.getProfileUuid());

        detailsPESAllerStruct.setPESPJ(pesAller.isPj() ? "1" : "0");
        detailsPESAllerStruct.setObjet(pesAller.getObjet());
        detailsPESAllerStruct.setUserName(JsonExtractorUtils.extractEmailFromProfile(node));
        detailsPESAllerStruct.setNomDocument(pesAller.getFileName());
        detailsPESAllerStruct.setDateDepot(dateFormatterWithMinutes.format(pesAller.getCreation()));
        detailsPESAllerStruct.setStatutBannette(pesAller.getLastHistoryStatus().name());

        List<PesHistory> fileHistories = pesAllerService.getPesHistoryByTypes(
                getDetailsPESAllerRequest.getIdPesAller(),
                Arrays.asList(StatusType.ACK_RECEIVED, StatusType.NACK_RECEIVED));

        Optional<PesHistory> peshistory = fileHistories.stream().findFirst();

        if (peshistory.isPresent()) {
            detailsPESAllerStruct.setDateAR(dateFormatter.format(peshistory.get().getDate()));
            detailsPESAllerStruct.setDateAnomalie("");
            detailsPESAllerStruct.setMotifAnomalie("");
        } else if (pesAllerService.hasAFatalError(pesAller)) {
            detailsPESAllerStruct.setDateAR("");
            detailsPESAllerStruct.setDateAnomalie(dateFormatterWithMinutes.format(pesAller.getLastHistoryDate()));
            detailsPESAllerStruct.setMotifAnomalie(pesAller.getLastHistoryStatus().name());
        } else {
            detailsPESAllerStruct.setDateAR("");
            detailsPESAllerStruct.setDateAnomalie("");
            detailsPESAllerStruct.setMotifAnomalie("");
        }

        // bunch of no longer used properties, set them all to an empty string
        detailsPESAllerStruct.setMotifPlusAnomalie("");
        detailsPESAllerStruct.setDateDepotBannette("");
        detailsPESAllerStruct.setStatutBannette("");
        detailsPESAllerStruct.setUserNameBannette("");
        detailsPESAllerStruct.setActeurCourant("");

        detailsPESAllerStruct.setMessage("SUCCESS");
        detailsPESAllerResponse.getRetour().add(detailsPESAllerStruct);
        detailsPESAllerResponse.setStatusCode("OK");
        return detailsPESAllerResponse;

    }

    private GetDetailsPESAllerResponse getDetailsPESAllerResponseError(String errorMessage) {
        GetDetailsPESAllerResponse detailsPESAllerResponse = new GetDetailsPESAllerResponse();
        GetDetailsPESAllerStruct detailsPESAllerStruct = new GetDetailsPESAllerStruct();
        detailsPESAllerStruct.setMessage(errorMessage);
        detailsPESAllerResponse.getRetour().add(detailsPESAllerStruct);
        detailsPESAllerResponse.setStatusCode("NOK");

        LOGGER.info("Returning error response : {}", detailsPESAllerStruct.getMessage());

        return detailsPESAllerResponse;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getPESAllerRequest")
    public @ResponsePayload
    GetPESAllerResponse getPESAller(@RequestPayload GetPESAllerRequest getPESAllerRequest)
            throws IOException {

        PaullSoapToken paullSoapToken = getToken(getPESAllerRequest.getSessionId());

        GetPESAllerResponse pesAllerResponse = new GetPESAllerResponse();

        GetPESAllerStruct pesAllerStruct = new GetPESAllerStruct();

        String returnMessage;
        String status = "NOK";
        if (paullSoapToken == null) {
            returnMessage = "SESSION_INVALID_OR_EXPIRED";
        } else {
            GenericAccount genericAccount = externalRestService.getGenericAccount(paullSoapToken.getAccountUuid());

            if (localAuthorityService.localAuthorityGranted(genericAccount, paullSoapToken.getSiren())) {

                try {
                    Pair<String, String> pesArchive =
                            pesAllerService.generatePesArchiveWithAck(getPESAllerRequest.getIdPesAller());

                    pesAllerStruct.setFilename(pesArchive.getLeft());
                    pesAllerStruct.setBase64(pesArchive.getRight());

                    returnMessage = "SUCCESS";
                    status = "OK";
                } catch (IOException e) {
                    returnMessage = "UNEXPECTED_ERROR";
                }
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
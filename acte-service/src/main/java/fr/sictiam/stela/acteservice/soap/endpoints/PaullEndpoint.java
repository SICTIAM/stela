package fr.sictiam.stela.acteservice.soap.endpoints;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.model.ActeHistory;
import fr.sictiam.stela.acteservice.model.ActeNature;
import fr.sictiam.stela.acteservice.model.Attachment;
import fr.sictiam.stela.acteservice.model.LocalAuthority;
import fr.sictiam.stela.acteservice.model.StatusType;
import fr.sictiam.stela.acteservice.model.ui.GenericAccount;
import fr.sictiam.stela.acteservice.service.ActeService;
import fr.sictiam.stela.acteservice.service.ExternalRestService;
import fr.sictiam.stela.acteservice.service.LocalAuthorityService;
import fr.sictiam.stela.acteservice.soap.model.paull.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.apache.commons.lang3.StringUtils;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Endpoint
public class PaullEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaullEndpoint.class);

    private static final String NAMESPACE_URI = "http://www.processmaker.com";

    @Value("${application.jwt.secret}")
    String SECRET;

    private final ActeService acteService;
    private final LocalAuthorityService localAuthorityService;
    private final SoapReturnGenerator soapReturnGenerator;
    private final ExternalRestService externalRestService;

    public PaullEndpoint(ActeService acteService, LocalAuthorityService localAuthorityService,
            SoapReturnGenerator soapReturnGenerator, ExternalRestService externalRestService) {
        this.localAuthorityService = localAuthorityService;
        this.externalRestService = externalRestService;
        this.acteService = acteService;
        this.soapReturnGenerator = soapReturnGenerator;
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

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "depotActeRequest")
    public @ResponsePayload DepotActeResponse depotActe(@RequestPayload DepotActeRequest depotActeRequest)
            throws IOException, Base64DecodingException {
        PaullSoapToken paullSoapToken = getToken(depotActeRequest.getSessionId());

        DepotActeResponse depotActeResponse = new DepotActeResponse();

        DepotActeStruct depotActeStruct = new DepotActeStruct();
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

                List<Attachment> attachments = depotActeRequest.getFichiers().stream().map(file -> {
                    byte[] byteArray = null;
                    String name = StringUtils.stripAccents(file.getFilename());
                    try {
                        byteArray = Base64.getDecoder().decode(file.getBase64().getBytes("UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        LOGGER.error(e.getMessage());
                    }
                    return new Attachment(byteArray, name, byteArray.length);
                }).collect(Collectors.toList());

                LocalAuthority currentLocalAuthority = localAuthorityService.getBySiren(paullSoapToken.getSiren())
                        .get();

                DepotActeStruct1 infosActes = depotActeRequest.getInfosActe().get(0);
                Attachment mainAttachement = attachments.remove(0);
                Acte acte = new Acte(infosActes.getNumInterne(), LocalDate.parse(infosActes.getDateDecision()),
                        ActeNature.code(Integer.parseInt(infosActes.getNatureActe())), infosActes.getMatiereActe(),
                        infosActes.getObjet(), false, false);

                acte.setActeAttachment(mainAttachement);
                acte.setAnnexes(attachments);
                acte.setCreation(LocalDateTime.now());

                acte.setLocalAuthority(currentLocalAuthority);
                acte.setProfileUuid(currentLocalAuthority.getGenericProfileUuid());
                acte.setCodeLabel(
                        localAuthorityService.getCodeMatiereLabel(currentLocalAuthority.getUuid(), acte.getCode()));
                Acte actePublished = acteService.publishActe(acte);
                status = "OK";
                returnMessage = "OK";
                depotActeStruct.setIdActe(actePublished.getUuid());
            }
        }
        depotActeResponse.setStatusCode(status);
        depotActeStruct.setMessage(returnMessage);
        depotActeResponse.getRetour().add(depotActeStruct);

        return depotActeResponse;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getDetailsActeRequest")
    public @ResponsePayload GetDetailsActeResponse getDetailsActe(
            @RequestPayload GetDetailsActeRequest getDetailsActeRequest) throws IOException {
        PaullSoapToken paullSoapToken = getToken(getDetailsActeRequest.getSessionId());

        GetDetailsActeResponse depotActeResponse = new GetDetailsActeResponse();

        GetDetailsActeStruct retour = new GetDetailsActeStruct();

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

                Acte acte = acteService.getByUuid(getDetailsActeRequest.getIdActe());

                Map<String, Object> returnMap = new HashMap<>();

                retour.setMiatID(acte.getMiatId());
                retour.setNumActe(acte.getNumber());
                retour.setObjet(acte.getObjet());
                try {
                    JsonNode node = externalRestService.getProfile(acte.getProfileUuid());
                    retour.setUserName(node.get("agent").get("email").asText());
                } catch (IOException e) {
                    LOGGER.error(e.getMessage());
                }
                retour.setNatureActe(acte.getNature().name());
                retour.setMatiereActe(acte.getCodeLabel());
                retour.setNomDocument(acte.getActeAttachment().getFilename());
                retour.setAnnexesList(acte.getAnnexes().stream().map(annexe -> annexe.getFilename())
                        .collect(Collectors.joining(";")));
                retour.setStatut(
                        acteService.getActeHistoryDefinition(acteService.getLastMetierHistory(acte.getUuid())));

                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss");
                retour.setDateDecision(dateFormatter.format(acte.getDecision()));

                Optional<ActeHistory> sentHistory = acteService.findFirstActeHistory(acte, StatusType.SENT);
                retour.setDateDepotActe(
                        sentHistory.isPresent() ? dateTimeFormatter.format(sentHistory.get().getDate()) : "");

                Optional<ActeHistory> arHistory = acteService.findFirstActeHistory(acte, StatusType.ACK_RECEIVED);
                retour.setDateAR(arHistory.isPresent() ? dateTimeFormatter.format(arHistory.get().getDate()) : "");

                Optional<ActeHistory> canceledHistory = acteService.findFirstActeHistory(acte, StatusType.CANCELLED);
                retour.setDateARAnnul(
                        canceledHistory.isPresent() ? dateTimeFormatter.format(canceledHistory.get().getDate()) : "");

                Optional<ActeHistory> nackHistory = acteService.findFirstActeHistory(acte, StatusType.NACK_RECEIVED);

                retour.setAnomalies(canceledHistory.isPresent() ? nackHistory.get().getMessage() : "");

                retour.setCourrierSimple(soapReturnGenerator.generateJson(acteService
                        .streamActeHistoriesByStatus(acte, StatusType.COURRIER_SIMPLE_RECEIVED).map(acteHistory -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("date_reception", dateTimeFormatter.format(acteHistory.getDate()));
                            map.put("nom_fichier", acteHistory.getFileName());

                            return map;
                        }).collect(Collectors.toList())));
                retour.setReponseCourrierSimple(soapReturnGenerator.generateJson(
                        acteService.streamActeHistoriesByStatus(acte, StatusType.REPONSE_COURRIER_SIMPLE_ASKED)
                                .map(acteHistory -> {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("form_id_courrier_simple", acteHistory.getUuid());
                                    map.put("date_reception", dateTimeFormatter.format(acteHistory.getDate()));

                                    return map;
                                }).collect(Collectors.toList())));
                retour.setDefer(soapReturnGenerator.generateJson(
                        acteService.streamActeHistoriesByStatus(acte, StatusType.DEFERE_RECEIVED).map(acteHistory -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("nature_illegalite", acteHistory.getMessage());
                            map.put("date_reception", dateTimeFormatter.format(acteHistory.getDate()));

                            return map;
                        }).collect(Collectors.toList())));

                retour.setLettreObservations(soapReturnGenerator.generateJson(acteService
                        .streamActeHistoriesByStatus(acte, StatusType.LETTRE_OBSERVATION_RECEIVED).map(acteHistory -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("form_id_lo", acteHistory.getUuid());
                            map.put("nom_fichier", acteHistory.getFileName());
                            map.put("motif", acteHistory.getMessage());
                            map.put("date_reception", dateTimeFormatter.format(acteHistory.getDate()));

                            return map;
                        }).collect(Collectors.toList())));

                retour.setReponseLettreObservations(soapReturnGenerator.generateJson(
                        acteService.streamActeHistoriesByStatus(acte, StatusType.REPONSE_LETTRE_OBSEVATION_ASKED)
                                .map(acteHistory -> {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("date_reception", dateTimeFormatter.format(acteHistory.getDate()));
                                    return map;
                                }).collect(Collectors.toList())));

                retour.setRefusLettreObservations(soapReturnGenerator.generateJson(
                        acteService.streamActeHistoriesByStatus(acte, StatusType.REJET_LETTRE_OBSERVATION_ASKED)
                                .map(acteHistory -> {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("date_reception", dateTimeFormatter.format(acteHistory.getDate()));
                                    return map;
                                }).collect(Collectors.toList())));

                retour.setDemandePC(soapReturnGenerator.generateJson(acteService
                        .streamActeHistoriesByStatus(acte, StatusType.PIECE_COMPLEMENTAIRE_ASKED).map(acteHistory -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("form_id_lo", acteHistory.getUuid());
                            map.put("nom_fichier", acteHistory.getFileName());
                            map.put("motif", acteHistory.getMessage());
                            map.put("date_reception", dateTimeFormatter.format(acteHistory.getDate()));

                            return map;
                        }).collect(Collectors.toList())));

                retour.setReponseDemandePC(soapReturnGenerator.generateJson(acteService
                        .streamActeHistoriesByStatus(acte, StatusType.PIECE_COMPLEMENTAIRE_ASKED).map(acteHistory -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("date_reception", dateTimeFormatter.format(acteHistory.getDate()));
                            return map;
                        }).collect(Collectors.toList())));

                retour.setRefusDemandePC(soapReturnGenerator.generateJson(
                        acteService.streamActeHistoriesByStatus(acte, StatusType.REFUS_PIECES_COMPLEMENTAIRE_ASKED)
                                .map(acteHistory -> {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("date_reception", dateTimeFormatter.format(acteHistory.getDate()));
                                    return map;
                                }).collect(Collectors.toList())));

                status = "OK";
                returnMessage = "OK";
            }
        }
        depotActeResponse.setStatusCode(status);
        retour.setMessage(returnMessage);
        depotActeResponse.getRetour().add(retour);

        return depotActeResponse;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getDocumentRequest")
    public @ResponsePayload GetDocumentResponse getDocument(@RequestPayload GetDocumentRequest getDocumentRequest)
            throws IOException {
        PaullSoapToken paullSoapToken = getToken(getDocumentRequest.getSessionId());

        GetDocumentResponse documentResponse = new GetDocumentResponse();

        GetDocumentStruct retour = new GetDocumentStruct();

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
                Acte acte = acteService.getByUuid(getDocumentRequest.getIdActe());

                byte[] document = acte.getActeAttachment().getFile();

                retour.setBase64(Base64.getEncoder().encodeToString(document));
                retour.setFilename(acte.getActeAttachment().getFilename());
                status = "OK";
                returnMessage = "OK";

            }
        }

        documentResponse.setStatusCode(status);
        retour.setMessage(returnMessage);
        documentResponse.getRetour().add(retour);

        return documentResponse;

    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getAnnexesRequest")
    public @ResponsePayload GetAnnexesResponse getAnnexes(@RequestPayload GetAnnexesRequest getAnnexesRequest)
            throws IOException {
        PaullSoapToken paullSoapToken = getToken(getAnnexesRequest.getSessionId());

        GetAnnexesResponse response = new GetAnnexesResponse();

        GetAnnexesStruct1 retour = new GetAnnexesStruct1();

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
                Acte acte = acteService.getByUuid(getAnnexesRequest.getIdActe());
                List<GetAnnexesStruct> annexes = acte.getAnnexes().stream().map(annexe -> {
                    GetAnnexesStruct annexeStruct = new GetAnnexesStruct();
                    annexeStruct.setBase64(Base64.getEncoder().encodeToString(annexe.getFile()));
                    annexeStruct.setFilename(acte.getActeAttachment().getFilename());
                    return new GetAnnexesStruct();
                }).collect(Collectors.toList());

                retour.getFichiers().addAll(annexes);
                status = "OK";
                returnMessage = "OK";

            }
        }

        response.setStatusCode(status);
        retour.setMessage(returnMessage);
        response.getRetour().add(retour);

        return response;

    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getClassificationActeRequest")
    public @ResponsePayload GetClassificationActeResponse getClassificationActe(
            @RequestPayload GetClassificationActeRequest getClassificationActeRequest) throws IOException {
        PaullSoapToken paullSoapToken = getToken(getClassificationActeRequest.getSessionId());

        GetClassificationActeResponse response = new GetClassificationActeResponse();

        GetClassificationActeStruct retour = new GetClassificationActeStruct();

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
                LocalAuthority currentLocalAuthority = localAuthorityService.getBySiren(paullSoapToken.getSiren())
                        .get();
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                retour.setCollectiviteDateClassification(
                        dateFormatter.format(currentLocalAuthority.getNomenclatureDate()));
                List<GetClassificationActeStruct1> materials = currentLocalAuthority.getMaterialCodes().stream()
                        .map(materialCode -> {
                            GetClassificationActeStruct1 material = new GetClassificationActeStruct1();
                            material.setCle(materialCode.getCode());
                            material.setValeur(materialCode.getLabel());
                            return material;
                        }).collect(Collectors.toList());
                retour.getCodeMatiere().addAll(materials);
                List<GetClassificationActeStruct1> natures = new ArrayList<>();
                for (ActeNature nature : ActeNature.values()) {
                    GetClassificationActeStruct1 natureStruct = new GetClassificationActeStruct1();
                    natureStruct.setCle(nature.getCode());
                    natureStruct.setValeur(nature.name());
                    natures.add(natureStruct);
                }
                retour.getNatureActes().addAll(natures);
                status = "OK";
                returnMessage = "OK";

            }
        }

        response.setStatusCode(status);
        retour.setMessage(returnMessage);
        response.setRetour(retour);

        return response;

    }

}
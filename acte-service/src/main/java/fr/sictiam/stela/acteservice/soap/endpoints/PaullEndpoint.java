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
import fr.sictiam.stela.acteservice.soap.model.paull.DepotActeRequest;
import fr.sictiam.stela.acteservice.soap.model.paull.DepotActeResponse;
import fr.sictiam.stela.acteservice.soap.model.paull.DepotActeStruct;
import fr.sictiam.stela.acteservice.soap.model.paull.DepotActeStruct1;
import fr.sictiam.stela.acteservice.soap.model.paull.GetDetailsActeRequest;
import fr.sictiam.stela.acteservice.soap.model.paull.GetDetailsActeResponse;
import fr.sictiam.stela.acteservice.soap.model.paull.GetDetailsActeStruct;
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

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "depotActeRequest")
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

                returnMap.put("id_acte", acte.getUuid());
                returnMap.put("num_acte", acte.getNumber());
                returnMap.put("miat_ID", acte.getMiatId());
                returnMap.put("precedent_acte_id", "");
                returnMap.put("objet", acte.getObjet());
                try {
                    JsonNode node = externalRestService.getProfile(acte.getProfileUuid());
                    returnMap.put("user_name", node.get("agent").get("email").asText());
                } catch (IOException e) {
                    LOGGER.error(e.getMessage());
                }
                returnMap.put("user_name_deposant_banette", "");
                returnMap.put("natureActe", acte.getNature().name());
                returnMap.put("matiereActe", acte.getCodeLabel());
                returnMap.put("nomDocument", acte.getActeAttachment().getFilename());
                returnMap.put("annexes_list", acte.getAnnexes().stream().map(annexe -> annexe.getFilename())
                        .collect(Collectors.joining(";")));
                returnMap.put("statut",
                        acteService.getActeHistoryDefinition(acteService.getLastMetierHistory(acte.getUuid())));

                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss");

                returnMap.put("dateDecision", dateFormatter.format(acte.getDecision()));
                returnMap.put("dateDepotActe", dateTimeFormatter.format(acte.getCreation()));

                Optional<ActeHistory> sentHistory = acteService.findFirstActeHistory(acte, StatusType.SENT);

                returnMap.put("dateEnvoiActe",
                        sentHistory.isPresent() ? dateTimeFormatter.format(sentHistory.get().getDate()) : "");

                returnMap.put("dateDepotBanette", "");

                Optional<ActeHistory> arHistory = acteService.findFirstActeHistory(acte, StatusType.ACK_RECEIVED);

                returnMap.put("dateAR",
                        arHistory.isPresent() ? dateTimeFormatter.format(arHistory.get().getDate()) : "");

                Optional<ActeHistory> canceledHistory = acteService.findFirstActeHistory(acte, StatusType.CANCELLED);

                returnMap.put("dateARAnnul",
                        canceledHistory.isPresent() ? dateTimeFormatter.format(canceledHistory.get().getDate()) : "");

                Optional<ActeHistory> nackHistory = acteService.findFirstActeHistory(acte, StatusType.NACK_RECEIVED);

                returnMap.put("anomalies", canceledHistory.isPresent() ? nackHistory.get().getMessage() : "");

                returnMap.put("courrier_simple", acteService
                        .streamActeHistoriesByStatus(acte, StatusType.COURRIER_SIMPLE_RECEIVED).map(acteHistory -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("date_reception", dateTimeFormatter.format(acteHistory.getDate()));
                            map.put("nom_fichier", acteHistory.getFileName());

                            return map;
                        }).collect(Collectors.toList()));

                returnMap.put("reponse_courrier_simple",
                        acteService.streamActeHistoriesByStatus(acte, StatusType.REPONSE_COURRIER_SIMPLE_ASKED)
                                .map(acteHistory -> {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("form_id_courrier_simple", acteHistory.getUuid());
                                    map.put("date_reception", dateTimeFormatter.format(acteHistory.getDate()));

                                    return map;
                                }).collect(Collectors.toList()));

                returnMap.put("defere",
                        acteService.streamActeHistoriesByStatus(acte, StatusType.DEFERE_RECEIVED).map(acteHistory -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("nature_illegalite", acteHistory.getMessage());
                            map.put("date_reception", dateTimeFormatter.format(acteHistory.getDate()));

                            return map;
                        }).collect(Collectors.toList()));

                returnMap.put("lettre_observations", acteService
                        .streamActeHistoriesByStatus(acte, StatusType.LETTRE_OBSERVATION_RECEIVED).map(acteHistory -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("form_id_lo", acteHistory.getUuid());
                            map.put("nom_fichier", acteHistory.getFileName());
                            map.put("motif", acteHistory.getMessage());
                            map.put("date_reception", dateTimeFormatter.format(acteHistory.getDate()));

                            return map;
                        }).collect(Collectors.toList()));

                returnMap.put("reponse_lettre_observations",
                        acteService.streamActeHistoriesByStatus(acte, StatusType.REPONSE_LETTRE_OBSEVATION_ASKED)
                                .map(acteHistory -> {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("date_reception", dateTimeFormatter.format(acteHistory.getDate()));
                                    return map;
                                }).collect(Collectors.toList()));

                returnMap.put("refus_lettre_observations",
                        acteService.streamActeHistoriesByStatus(acte, StatusType.REJET_LETTRE_OBSERVATION_ASKED)
                                .map(acteHistory -> {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("date_reception", dateTimeFormatter.format(acteHistory.getDate()));
                                    return map;
                                }).collect(Collectors.toList()));

                returnMap.put("demande_pc", acteService
                        .streamActeHistoriesByStatus(acte, StatusType.PIECE_COMPLEMENTAIRE_ASKED).map(acteHistory -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("form_id_lo", acteHistory.getUuid());
                            map.put("nom_fichier", acteHistory.getFileName());
                            map.put("motif", acteHistory.getMessage());
                            map.put("date_reception", dateTimeFormatter.format(acteHistory.getDate()));

                            return map;
                        }).collect(Collectors.toList()));

                returnMap.put("reponse_demande_pc", acteService
                        .streamActeHistoriesByStatus(acte, StatusType.PIECE_COMPLEMENTAIRE_ASKED).map(acteHistory -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("date_reception", dateTimeFormatter.format(acteHistory.getDate()));
                            return map;
                        }).collect(Collectors.toList()));

                returnMap.put("refus_demande_pc",
                        acteService.streamActeHistoriesByStatus(acte, StatusType.REFUS_PIECES_COMPLEMENTAIRE_ASKED)
                                .map(acteHistory -> {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("date_reception", dateTimeFormatter.format(acteHistory.getDate()));
                                    return map;
                                }).collect(Collectors.toList()));

                status = "OK";
                returnMessage = "OK";
            }
        }
        depotActeResponse.setStatusCode(status);
        retour.setMessage(returnMessage);
        depotActeResponse.getRetour().add(retour);

        return depotActeResponse;
    }

}
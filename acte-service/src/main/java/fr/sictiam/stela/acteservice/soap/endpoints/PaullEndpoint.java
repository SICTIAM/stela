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
import io.vavr.control.Either;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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
                return objectMapper.readValue(tokenParsed, PaullSoapToken.class);
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
                return null;
            }

        }
        return null;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "depotActeRequest")
    public @ResponsePayload DepotActeResponse depotActe(@RequestPayload DepotActeRequest depotActeRequest)
            throws IOException {

        Either<String, LocalAuthority> localAuthorityCheckResult =
                checkAndGetLocalAuthority(depotActeRequest.getSessionId());
        if (localAuthorityCheckResult.isLeft())
            return depotActeResponseError(localAuthorityCheckResult.getLeft());

        LocalAuthority localAuthority = localAuthorityCheckResult.get();

        DepotActeResponse depotActeResponse = new DepotActeResponse();
        DepotActeStruct depotActeStruct = new DepotActeStruct();

        List<Attachment> attachments = depotActeRequest.getFichiers().stream().map(file -> {
            String name = StringUtils.stripAccents(file.getFilename());
            byte[] byteArray = Base64.getDecoder().decode(file.getBase64().getBytes(StandardCharsets.UTF_8));
            return new Attachment(byteArray, name, byteArray.length);
        }).collect(Collectors.toList());

        DepotActeStruct1 infosActes = depotActeRequest.getInfosActe().get(0);
        Attachment mainAttachement = attachments.remove(0);
        Acte acte = new Acte(infosActes.getNumInterne(), LocalDate.from(dateFormatter.parse(infosActes.getDateDecision())),
                ActeNature.code(Integer.parseInt(infosActes.getNatureActe())), infosActes.getMatiereActe(),
                infosActes.getObjet(), false, false);

        acte.setActeAttachment(mainAttachement);
        acte.setAnnexes(attachments);
        acte.setCreation(LocalDateTime.now());

        String currentProfileUuid;
        if (StringUtils.isEmpty(infosActes.getEmail())) {
            currentProfileUuid = localAuthority.getGenericProfileUuid();
        } else {
            JsonNode jsonNode = externalRestService.getProfileByLocalAuthoritySirenAndEmail(
                    localAuthority.getSiren(), infosActes.getEmail());
            currentProfileUuid = jsonNode.get("uuid").asText();
        }

        acte.setLocalAuthority(localAuthority);
        acte.setProfileUuid(currentProfileUuid);
        acte.setCodeLabel(localAuthorityService.getCodeMatiereLabel(localAuthority.getUuid(), acte.getCode()));
        Acte actePublished = acteService.publishActe(acte);

        depotActeResponse.setStatusCode("OK");
        depotActeStruct.setIdActe(actePublished.getUuid());
        depotActeStruct.setMessage("OK");
        depotActeResponse.getRetour().add(depotActeStruct);

        return depotActeResponse;
    }

    private DepotActeResponse depotActeResponseError(String errorMessage) {
        DepotActeResponse depotActeResponse = new DepotActeResponse();
        DepotActeStruct depotActeStruct = new DepotActeStruct();

        depotActeResponse.setStatusCode("NOK");
        depotActeStruct.setMessage(errorMessage);
        depotActeResponse.getRetour().add(depotActeStruct);

        LOGGER.error("Returning error response for depot acte : {}", errorMessage);

        return depotActeResponse;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getDetailsActeRequest")
    public @ResponsePayload GetDetailsActeResponse getDetailsActe(
            @RequestPayload GetDetailsActeRequest getDetailsActeRequest) throws IOException {

        Either<String, LocalAuthority> localAuthorityCheckResult =
                checkAndGetLocalAuthority(getDetailsActeRequest.getSessionId());
        if (localAuthorityCheckResult.isLeft())
            return detailsActeResponseError(localAuthorityCheckResult.getLeft());

        GetDetailsActeResponse depotActeResponse = new GetDetailsActeResponse();
        GetDetailsActeStruct retour = new GetDetailsActeStruct();

        Acte acte = acteService.getByUuid(getDetailsActeRequest.getIdActe());

        retour.setMiatID(acte.getMiatId());
        retour.setNumActe(acte.getNumber());
        retour.setObjet(acte.getObjet());
        try {
            JsonNode node = externalRestService.getProfile(acte.getProfileUuid());
            retour.setUserName(node.get("agent").get("email").asText());
        } catch (IOException e) {
            LOGGER.error("Unable to retrieve agent email : {}", e.getMessage());
        }
        retour.setNatureActe(acte.getNature().name());
        retour.setMatiereActe(acte.getCodeLabel());
        retour.setNomDocument(acte.getActeAttachment().getFilename());
        retour.setAnnexesList(acte.getAnnexes().stream().map(Attachment::getFilename)
                .collect(Collectors.joining(";")));
        retour.setStatut(
                acteService.getActeHistoryDefinition(acteService.getLastMetierHistory(acte.getUuid())));

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss");
        retour.setDateDecision(dateFormatter.format(acte.getDecision()));

        // TODO : make one query to retrieve the whole Acte history ...

        Optional<ActeHistory> sentHistory = acteService.findFirstActeHistory(acte, StatusType.SENT);
        retour.setDateDepotActe(
                sentHistory.map(history -> dateTimeFormatter.format(history.getDate())).orElse(""));

        Optional<ActeHistory> arHistory = acteService.findFirstActeHistory(acte, StatusType.ACK_RECEIVED);
        retour.setDateAR(arHistory.map(history -> dateTimeFormatter.format(history.getDate())).orElse(""));

        Optional<ActeHistory> canceledHistory = acteService.findFirstActeHistory(acte, StatusType.CANCELLED);
        retour.setDateARAnnul(
                canceledHistory.map(history -> dateTimeFormatter.format(history.getDate())).orElse(""));

        Optional<ActeHistory> nackHistory = acteService.findFirstActeHistory(acte, StatusType.NACK_RECEIVED);
        retour.setAnomalies(nackHistory.isPresent() ? nackHistory.get().getMessage() : "");

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

        depotActeResponse.setStatusCode("OK");
        retour.setMessage("OK");
        depotActeResponse.getRetour().add(retour);

        return depotActeResponse;
    }

    private GetDetailsActeResponse detailsActeResponseError(String errorMessage) {
        GetDetailsActeResponse getDetailsActeResponse = new GetDetailsActeResponse();
        GetDetailsActeStruct getDetailsActeStruct = new GetDetailsActeStruct();

        getDetailsActeResponse.setStatusCode("NOK");
        getDetailsActeStruct.setMessage(errorMessage);
        getDetailsActeResponse.getRetour().add(getDetailsActeStruct);

        LOGGER.error("Returning error response for get details acte : {}", errorMessage);

        return getDetailsActeResponse;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getDocumentRequest")
    public @ResponsePayload GetDocumentResponse getDocument(@RequestPayload GetDocumentRequest getDocumentRequest)
            throws IOException {

        Either<String, LocalAuthority> localAuthorityCheckResult =
                checkAndGetLocalAuthority(getDocumentRequest.getSessionId());
        if (localAuthorityCheckResult.isLeft())
            return documentResponseError(localAuthorityCheckResult.getLeft());

        GetDocumentResponse documentResponse = new GetDocumentResponse();
        GetDocumentStruct retour = new GetDocumentStruct();

        Acte acte = acteService.getByUuid(getDocumentRequest.getIdActe());

        byte[] document = acte.getActeAttachment().getFile();
        retour.setBase64(Base64.getEncoder().encodeToString(document));
        retour.setFilename(acte.getActeAttachment().getFilename());

        documentResponse.setStatusCode("OK");
        retour.setMessage("OK");
        documentResponse.getRetour().add(retour);

        return documentResponse;
    }

    private GetDocumentResponse documentResponseError(String errorMessage) {
        GetDocumentResponse getDocumentResponse = new GetDocumentResponse();
        GetDocumentStruct getDocumentStruct = new GetDocumentStruct();

        getDocumentResponse.setStatusCode("NOK");
        getDocumentStruct.setMessage(errorMessage);
        getDocumentResponse.getRetour().add(getDocumentStruct);

        LOGGER.error("Returning error response for get acte document : {}", errorMessage);

        return getDocumentResponse;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getAnnexesRequest")
    public @ResponsePayload GetAnnexesResponse getAnnexes(@RequestPayload GetAnnexesRequest getAnnexesRequest)
            throws IOException {

        Either<String, LocalAuthority> localAuthorityCheckResult =
                checkAndGetLocalAuthority(getAnnexesRequest.getSessionId());
        if (localAuthorityCheckResult.isLeft())
            return annexesResponseError(localAuthorityCheckResult.getLeft());

        GetAnnexesResponse response = new GetAnnexesResponse();
        GetAnnexesStruct1 retour = new GetAnnexesStruct1();

        Acte acte = acteService.getByUuid(getAnnexesRequest.getIdActe());
        List<GetAnnexesStruct> annexes = acte.getAnnexes().stream().map(annexe -> {
            GetAnnexesStruct annexeStruct = new GetAnnexesStruct();
            annexeStruct.setBase64(Base64.getEncoder().encodeToString(annexe.getFile()));
            annexeStruct.setFilename(acte.getActeAttachment().getFilename());
            return new GetAnnexesStruct();
        }).collect(Collectors.toList());

        retour.getFichiers().addAll(annexes);

        response.setStatusCode("OK");
        retour.setMessage("OK");
        response.getRetour().add(retour);

        return response;
    }

    private GetAnnexesResponse annexesResponseError(String errorMessage) {
        GetAnnexesResponse getAnnexesResponse = new GetAnnexesResponse();
        GetAnnexesStruct1 getAnnexesStruct1 = new GetAnnexesStruct1();

        getAnnexesResponse.setStatusCode("NOK");
        getAnnexesStruct1.setMessage(errorMessage);
        getAnnexesResponse.getRetour().add(getAnnexesStruct1);

        LOGGER.error("Returning error response for get acte annexes : {}", errorMessage);

        return getAnnexesResponse;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getClassificationActeRequest")
    public @ResponsePayload GetClassificationActeResponse getClassificationActe(
            @RequestPayload GetClassificationActeRequest getClassificationActeRequest) throws IOException {

        Either<String, LocalAuthority> localAuthorityCheckResult =
                checkAndGetLocalAuthority(getClassificationActeRequest.getSessionId());
        if (localAuthorityCheckResult.isLeft())
            return classificationActeResponseError(localAuthorityCheckResult.getLeft());

        LocalAuthority localAuthority = localAuthorityCheckResult.get();
        GetClassificationActeResponse response = new GetClassificationActeResponse();
        GetClassificationActeStruct retour = new GetClassificationActeStruct();

        if (localAuthority.getNomenclatureDate() == null)
            return classificationActeResponseError("NO_NOMENCLATURE_FOR_LOCALAUTHORITY");

        retour.setCollectiviteDateClassification(dateFormatter.format(localAuthority.getNomenclatureDate()));
        List<GetClassificationActeStruct1> materials = localAuthority.getMaterialCodes().stream()
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

        response.setStatusCode("OK");
        retour.setMessage("OK");
        response.setRetour(retour);

        return response;
    }

    private Either<String, LocalAuthority> checkAndGetLocalAuthority(String sessionId) throws IOException {
        PaullSoapToken paullSoapToken = getToken(sessionId);
        if (paullSoapToken == null)
            return Either.left("SESSION_INVALID_OR_EXPIRED");

        GenericAccount genericAccount = externalRestService.getGenericAccount(paullSoapToken.getAccountUuid());

        if (!localAuthorityService.localAuthorityGranted(genericAccount, paullSoapToken.getSiren()))
            return Either.left("LOCALAUTHORITY_NOT_GRANTED");

        Optional<LocalAuthority> optionalLocalAuthority =
                localAuthorityService.getBySirenWithMaterialCodes(paullSoapToken.getSiren());
        if (!optionalLocalAuthority.isPresent())
            return Either.left("LOCALAUTHORITY_NOT_FOUND_FOR_SIREN");

        return Either.right(optionalLocalAuthority.get());
    }

    private GetClassificationActeResponse classificationActeResponseError(String errorMessage) {
        GetClassificationActeResponse response = new GetClassificationActeResponse();
        GetClassificationActeStruct retour = new GetClassificationActeStruct();

        response.setStatusCode("NOK");
        retour.setMessage(errorMessage);
        response.setRetour(retour);

        LOGGER.error("Returning error response for classification acte : {}", errorMessage);
        return response;
    }

}
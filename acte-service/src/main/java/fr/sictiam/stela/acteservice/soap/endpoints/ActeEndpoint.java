package fr.sictiam.stela.acteservice.soap.endpoints;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.DocumentException;
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
import fr.sictiam.stela.acteservice.service.exceptions.ActeNotFoundException;
import fr.sictiam.stela.acteservice.service.exceptions.CancelForbiddenException;
import fr.sictiam.stela.acteservice.soap.model.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.server.endpoint.annotation.SoapHeader;
import org.w3c.dom.Node;

import javax.xml.transform.dom.DOMResult;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Endpoint
public class ActeEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActeEndpoint.class);

    private static final String NAMESPACE_URI = "urn:myInputNamespace";

    private final ActeService acteService;
    private final LocalAuthorityService localAuthorityService;
    private final SoapReturnGenerator soapReturnGenerator;
    private final ExternalRestService externalRestService;
    private ObjectFactory objectFactory;

    public ActeEndpoint(ActeService acteService, LocalAuthorityService localAuthorityService,
            SoapReturnGenerator soapReturnGenerator, ExternalRestService externalRestService) {
        this.acteService = acteService;
        this.localAuthorityService = localAuthorityService;
        this.soapReturnGenerator = soapReturnGenerator;
        this.externalRestService = externalRestService;
        this.objectFactory = new ObjectFactory();
    }

    public AuthHeader extractHeader(SoapHeaderElement soapHeaderElement) {
        String sslCertificatSerial = "";
        String sslCertificatVendor = "";
        DOMResult result = (DOMResult) soapHeaderElement.getResult();
        result.getNode().getChildNodes().getLength();
        for (int i = 0; i < result.getNode().getChildNodes().getLength(); i++) {
            Node node = result.getNode().getChildNodes().item(i);

            String key = node.getFirstChild().getTextContent();
            String value = node.getLastChild().getTextContent();
            if ("SSLCertificatSerial".equals(key)) {
                sslCertificatSerial = value;
            } else if ("SSLCertificatVendor".equals(key)) {
                sslCertificatVendor = value;
            }
        }
        return new AuthHeader(sslCertificatSerial, sslCertificatVendor);
    }

    public GenericAccount certificateAuth(SoapHeaderElement soapHeaderElement) {

        GenericAccount genericAccount = null;
        try {
            AuthHeader authHeader = extractHeader(soapHeaderElement);
            genericAccount = externalRestService.authWithCertificate(authHeader.getSerial(), authHeader.getVendor());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        return genericAccount;
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

    public GenericAccount emailOrCertificateAuth(SoapHeaderElement soapHeaderElement, String email, String password) {

        if (StringUtils.isNotBlank(email) && StringUtils.isNotBlank(password)) {
            return emailAuth(email, password);
        } else {
            return certificateAuth(soapHeaderElement);
        }
    }

    public boolean localAuthorityGranted(GenericAccount genericAccount, String localAuthorityUuid) {

        return genericAccount.getLocalAuthorities().stream()
                .anyMatch(localAuthority -> localAuthority.getActivatedModules().contains("ACTES")
                        && localAuthority.getUuid().equals(localAuthorityUuid));
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "connexionSTELA")
    public @ResponsePayload ConnexionSTELAOutput connexionSTELA(@RequestPayload ConnexionSTELAInput connexionSTELAInput,
            @SoapHeader(value = "{https://stela.sictiam.fr/}authHeader") SoapHeaderElement soapHeaderElement) {
        String typeRetour = connexionSTELAInput.getTypeRetour();
        if (StringUtils.isEmpty(typeRetour)) {
            typeRetour = "uid";
        }

        GenericAccount account = certificateAuth(soapHeaderElement);

        ConnexionSTELAOutput returnObject = objectFactory.createConnexionSTELAOutput();

        if (account != null) {
            if ("details".equals(typeRetour)) {
                Map<String, String> details = new HashMap<>();
                details.put("uid", account.getUuid());
                details.put("loginname", account.getSoftware());
                details.put("name", "generic");
                details.put("email", account.getEmail());
                returnObject.setJsonConnexionSTELA(soapReturnGenerator.generateReturn("OK", details));

            } else if ("uid".equals(typeRetour)) {
                returnObject.setJsonConnexionSTELA(soapReturnGenerator.generateReturn("OK", account.getUuid()));
            } else {
                returnObject.setJsonConnexionSTELA(
                        soapReturnGenerator.generateReturn("NOK", "Zone typeRetour inexistante"));
            }
        } else {
            returnObject.setJsonConnexionSTELA(
                    soapReturnGenerator.generateReturn("NOK", "Certifcat non autorisé ou invalide"));
        }

        return returnObject;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "connexionSTELA2")
    public @ResponsePayload ConnexionSTELAOutput2 connexionSTELA2(
            @RequestPayload ConnexionSTELAInput2 getConnexionSTELA2) {

        ConnexionSTELAOutput2 returnObject = objectFactory.createConnexionSTELAOutput2();

        GenericAccount genericAccount = emailAuth(getConnexionSTELA2.getEmail(), getConnexionSTELA2.getPassword());

        if (genericAccount != null) {
            returnObject.setJsonConnexionSTELA2(soapReturnGenerator.generateReturn("OK", genericAccount.getUuid()));
        } else {
            returnObject.setJsonConnexionSTELA2(
                    soapReturnGenerator.generateReturn("NOK", "Email ou mot de passe invalide"));
        }

        return returnObject;

    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getRetoursPrefecture")
    @ResponsePayload
    public GetRetoursPrefectureOutput getRetoursPrefecture(
            @RequestPayload GetRetoursPrefectureInput getRetoursPrefectureInput,
            @SoapHeader(value = "{https://stela.sictiam.fr/}authHeader") SoapHeaderElement soapHeaderElement) {

        GetRetoursPrefectureOutput returnObject = objectFactory.createGetRetoursPrefectureOutput();

        GenericAccount account = certificateAuth(soapHeaderElement);
        if (account == null) {
            returnObject.setJsonGetRetoursPrefecture(soapReturnGenerator.connectionFailedReturn());
            return returnObject;
        }

        if (!localAuthorityGranted(account, getRetoursPrefectureInput.getGroupeid())) {
            returnObject.setJsonGetRetoursPrefecture(soapReturnGenerator.grantErrorReturn());
            return returnObject;
        }

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate localDate = LocalDate.parse(getRetoursPrefectureInput.getDate(), dateTimeFormatter);

        List<ActeHistory> acteHistories = acteService.getPrefectureReturns(getRetoursPrefectureInput.getGroupeid(),
                localDate.atStartOfDay());

        List<List<String>> outputArray = acteHistories.stream()
                .map(acteHistory -> acteService.getActeHistoryDefinitions(acteHistory)).collect(Collectors.toList());

        returnObject.setJsonGetRetoursPrefecture(soapReturnGenerator.generateReturn("OK", outputArray));

        return returnObject;

    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getResultatFormMiat")
    @ResponsePayload
    public GetResultatFormMiatOutput getResultatFormMiat(
            @RequestPayload GetResultatFormMiatInput getResultatFormMiatInput,
            @SoapHeader(value = "{https://stela.sictiam.fr/}authHeader") SoapHeaderElement soapHeaderElement) {

        GetResultatFormMiatOutput returnObject = objectFactory.createGetResultatFormMiatOutput();

        GenericAccount account = emailOrCertificateAuth(soapHeaderElement, getResultatFormMiatInput.getMail(),
                getResultatFormMiatInput.getPwd());
        if (account == null) {
            returnObject.setJsonGetResultatFormMiat(soapReturnGenerator.connectionFailedReturn());
            return returnObject;
        }

        Map<String, Object> profileSelected = new HashMap<>();

        List<Map<String, String>> grantedGroups = account.getLocalAuthorities().stream()
                .filter(localAuthority -> localAuthority.getActivatedModules().contains("ACTES"))
                .map(localAuthority -> {
                    Map<String, String> groupMap = new HashMap<>();
                    groupMap.put("groupid", localAuthority.getUuid());
                    groupMap.put("name", localAuthority.getName());
                    return groupMap;
                }).collect(Collectors.toList());
        if (!getResultatFormMiatInput.getGroupeid().equals("0")) {
            localAuthorityService.generateFormMiatReturn(
                    account.getLocalAuthorities().stream()
                            .filter(localAuthority -> localAuthority.getUuid()
                                    .equals(getResultatFormMiatInput.getGroupeid()))
                            .findFirst().get().getUuid(),
                    profileSelected);
        } else {
            localAuthorityService.generateFormMiatReturn(
                    account.getLocalAuthorities().stream().findFirst().get().getUuid(), profileSelected);
        }
        profileSelected.put("grantSendGroups", grantedGroups);

        returnObject.setJsonGetResultatFormMiat(soapReturnGenerator.generateReturn("OK", profileSelected));

        return returnObject;

    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getDocument")
    @ResponsePayload
    public GetDocumentOutput getDocument(@RequestPayload GetDocumentInput documentInput,
            @SoapHeader(value = "{https://stela.sictiam.fr/}authHeader") SoapHeaderElement soapHeaderElement) {

        GetDocumentOutput returnObject = objectFactory.createGetDocumentOutput();

        GenericAccount account = emailOrCertificateAuth(soapHeaderElement, documentInput.getMail(),
                documentInput.getPwd());
        if (account == null) {
            returnObject.setJsonGetDocument(soapReturnGenerator.connectionFailedReturn());
            return returnObject;
        }

        Acte acte = acteService.findByIdActe(documentInput.getMiatID());

        byte[] document = null;
        if ("1".equals(documentInput.getTampon()) && acteService.isActeACK(acte)) {
            returnObject.setJsonGetDocument(
                    soapReturnGenerator.generateReturn("NOK", "_ERROR_ACTE_SANS_AR_TAMPON_IMPOSSIBLE"));
            return returnObject;
        } else if ("1".equals(documentInput.getTampon())
                && FilenameUtils.getExtension(acte.getActeAttachment().getFilename()).equals("pdf")) {
            try {
                document = acteService.getStampedActe(acte, null, null, acte.getLocalAuthority());
            } catch (IOException | DocumentException e) {
                returnObject.setJsonGetDocument(soapReturnGenerator.generateReturn("NOK", "Unexpectederror"));
                return returnObject;
            }
        } else {
            document = acte.getActeAttachment().getFile();
        }
        document = Base64.getEncoder().encode(document);
        Map<String, Object> returnMap = new HashMap<>();
        returnMap.put("filename", acte.getActeAttachment().getFilename());
        returnMap.put("chaine_fichier", document);

        returnObject.setJsonGetDocument(soapReturnGenerator.generateReturn("OK", returnMap));
        return returnObject;

    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getAnnexes")
    @ResponsePayload
    public GetAnnexesOutput getAnnexes(@RequestPayload GetAnnexesInput annexesInput,
            @SoapHeader(value = "{https://stela.sictiam.fr/}authHeader") SoapHeaderElement soapHeaderElement) {

        GetAnnexesOutput returnObject = objectFactory.createGetAnnexesOutput();
        GenericAccount account = emailOrCertificateAuth(soapHeaderElement, annexesInput.getMail(),
                annexesInput.getPwd());
        if (account == null) {
            returnObject.setJsonGetAnnexes(soapReturnGenerator.connectionFailedReturn());
            return returnObject;
        }

        Acte acte = acteService.findByIdActe(annexesInput.getMiatID());

        if ("1".equals(annexesInput.getTampon()) && acteService.isActeACK(acte)) {
            returnObject.setJsonGetAnnexes(
                    soapReturnGenerator.generateReturn("NOK", "_ERROR_ACTE_SANS_AR_TAMPON_IMPOSSIBLE"));
            return returnObject;
        }
        List<Map<String, Object>> returnArray = new ArrayList<>();
        acte.getAnnexes().forEach(annexe -> {
            byte[] document = null;
            if ("1".equals(annexesInput.getTampon())
                    && FilenameUtils.getExtension(acte.getActeAttachment().getFilename()).equals("pdf")) {
                try {
                    document = acteService.getStampedAnnexe(acte, annexe, null, null, acte.getLocalAuthority());
                } catch (IOException | DocumentException e) {
                    LOGGER.error(e.getMessage());
                }
            } else {
                document = annexe.getFile();
            }
            document = Base64.getEncoder().encode(document);
            Map<String, Object> returnMap = new HashMap<>();
            returnMap.put("filename", annexe.getFilename());
            returnMap.put("chaine_fichier", document);
            returnArray.add(returnMap);
        });

        returnObject.setJsonGetAnnexes(soapReturnGenerator.generateReturn("OK", returnArray));
        return returnObject;

    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getDocumentComplementaire")
    @ResponsePayload
    public GetDocumentComplementaireOutput getDocumentComplementaire(
            @RequestPayload GetDocumentComplementaireInput documentComplementaireInput,
            @SoapHeader(value = "{https://stela.sictiam.fr/}authHeader") SoapHeaderElement soapHeaderElement) {

        GetDocumentComplementaireOutput returnObject = objectFactory.createGetDocumentComplementaireOutput();

        GenericAccount account = certificateAuth(soapHeaderElement);
        if (account == null) {
            returnObject.setJsonGetDocumentComplementaire(soapReturnGenerator.connectionFailedReturn());
            return returnObject;
        }

        ActeHistory acteHistory = acteService.getHistoryByUuid(documentComplementaireInput.getFormIdDoc());
        Map<String, Object> returnMap = new HashMap<>();

        returnMap.put("type_document", acteService.getActeHistoryDefinition(acteHistory));
        returnMap.put("filename", acteHistory.getFileName());
        returnMap.put("chaine_fichier", acteHistory.getFile());

        returnObject.setJsonGetDocumentComplementaire(soapReturnGenerator.generateReturn("OK", returnMap));
        return returnObject;

    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getDetailsActe")
    @ResponsePayload
    public GetDetailsActeOutput getDetailsActe(@RequestPayload GetDetailsActeInput detailsActeInput,
            @SoapHeader(value = "{https://stela.sictiam.fr/}authHeader") SoapHeaderElement soapHeaderElement) {

        GetDetailsActeOutput returnObject = objectFactory.createGetDetailsActeOutput();

        GenericAccount account = emailOrCertificateAuth(soapHeaderElement, detailsActeInput.getMail(),
                detailsActeInput.getPwd());

        if (account == null) {
            returnObject.setJsonGetDetailsActe(soapReturnGenerator.connectionFailedReturn());
            return returnObject;
        }

        Acte acte = acteService.findByIdActe(detailsActeInput.getMiatID());

        Map<String, Object> returnMap = new HashMap<>();

        returnMap.put("id_acte", acte.getUuid());
        returnMap.put("num_acte", acte.getNumber());
        returnMap.put("miat_ID", detailsActeInput.getMiatID());
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
        returnMap.put("annexes_list",
                acte.getAnnexes().stream().map(annexe -> annexe.getFilename()).collect(Collectors.joining(";")));
        returnMap.put("statut", acteService.getActeHistoryDefinition(acteService.getLastMetierHistory(acte.getUuid())));

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss");

        returnMap.put("dateDecision", dateFormatter.format(acte.getDecision()));
        returnMap.put("dateDepotActe", dateTimeFormatter.format(acte.getCreation()));

        Optional<ActeHistory> sentHistory = acteService.findFirstActeHistory(acte, StatusType.SENT);

        returnMap.put("dateEnvoiActe",
                sentHistory.isPresent() ? dateTimeFormatter.format(sentHistory.get().getDate()) : "");

        returnMap.put("dateDepotBanette", "");

        Optional<ActeHistory> arHistory = acteService.findFirstActeHistory(acte, StatusType.ACK_RECEIVED);

        returnMap.put("dateAR", arHistory.isPresent() ? dateTimeFormatter.format(arHistory.get().getDate()) : "");

        Optional<ActeHistory> canceledHistory = acteService.findFirstActeHistory(acte, StatusType.CANCELLED);

        returnMap.put("dateARAnnul",
                canceledHistory.isPresent() ? dateTimeFormatter.format(canceledHistory.get().getDate()) : "");

        Optional<ActeHistory> nackHistory = acteService.findFirstActeHistory(acte, StatusType.NACK_RECEIVED);

        returnMap.put("anomalies", canceledHistory.isPresent() ? nackHistory.get().getMessage() : "");

        returnMap.put("courrier_simple",
                acteService.streamActeHistoriesByStatus(acte, StatusType.COURRIER_SIMPLE_RECEIVED).map(acteHistory -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date_reception", dateTimeFormatter.format(acteHistory.getDate()));
                    map.put("nom_fichier", acteHistory.getFileName());

                    return map;
                }).collect(Collectors.toList()));

        returnMap.put("reponse_courrier_simple", acteService
                .streamActeHistoriesByStatus(acte, StatusType.REPONSE_COURRIER_SIMPLE_ASKED).map(acteHistory -> {
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

        returnMap.put("reponse_lettre_observations", acteService
                .streamActeHistoriesByStatus(acte, StatusType.REPONSE_LETTRE_OBSEVATION_ASKED).map(acteHistory -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date_reception", dateTimeFormatter.format(acteHistory.getDate()));
                    return map;
                }).collect(Collectors.toList()));

        returnMap.put("refus_lettre_observations", acteService
                .streamActeHistoriesByStatus(acte, StatusType.REJET_LETTRE_OBSERVATION_ASKED).map(acteHistory -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date_reception", dateTimeFormatter.format(acteHistory.getDate()));
                    return map;
                }).collect(Collectors.toList()));

        returnMap.put("demande_pc", acteService.streamActeHistoriesByStatus(acte, StatusType.PIECE_COMPLEMENTAIRE_ASKED)
                .map(acteHistory -> {
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

        returnMap.put("refus_demande_pc", acteService
                .streamActeHistoriesByStatus(acte, StatusType.REFUS_PIECES_COMPLEMENTAIRE_ASKED).map(acteHistory -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date_reception", dateTimeFormatter.format(acteHistory.getDate()));
                    return map;
                }).collect(Collectors.toList()));

        returnObject.setJsonGetDetailsActe(soapReturnGenerator.generateReturn("OK", returnMap));
        return returnObject;

    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getListeDeliberations")
    @ResponsePayload
    public GetListeDeliberationsOutput getListeDeliberations(
            @RequestPayload GetListeDeliberationsInput listeDeliberationsInput,
            @SoapHeader(value = "{https://stela.sictiam.fr/}authHeader") SoapHeaderElement soapHeaderElement) {

        GetListeDeliberationsOutput listeDeliberationsOutput = objectFactory.createGetListeDeliberationsOutput();

        GenericAccount account = certificateAuth(soapHeaderElement);
        if (account == null) {
            listeDeliberationsOutput.setJsonGetListeDeliberations(soapReturnGenerator.connectionFailedReturn());
            return listeDeliberationsOutput;
        }

        LocalDate decisionFrom = null;
        LocalDate decisionTo = null;

        if (!StringUtils.isEmpty(listeDeliberationsInput.getFiltreAnnee())) {
            int year = Integer.parseInt(listeDeliberationsInput.getFiltreAnnee());
            decisionFrom = LocalDate.ofYearDay(year, 0);
            decisionTo = LocalDate.ofYearDay(year + 1, 0);
        }
        String direction = "DESC";
        if ("asc".equalsIgnoreCase(listeDeliberationsInput.getOrder())) {
            direction = "ASC";
        }

        ActeNature nature = StringUtils.isEmpty(listeDeliberationsInput.getFiltreNature()) ? null
                : ActeNature.code(Integer.parseInt(listeDeliberationsInput.getFiltreNature()));

        Long count = acteService.countAllWithQuery(listeDeliberationsInput.getChampRecherche(), null, null, nature,
                decisionFrom, decisionTo, null, null, Collections.singleton(listeDeliberationsInput.getGroupe()));
        String sort = "";
        // TODO analyse input data
        switch (listeDeliberationsInput.getSort()) {
            case "objet":
                sort = "objet";
                break;

            default:
                break;
        }
        int start = Integer.parseInt(listeDeliberationsInput.getStart());
        int rowNumber = Integer.parseInt(listeDeliberationsInput.getPerPage());

        List<Acte> actes = acteService.getAllWithQuery(listeDeliberationsInput.getChampRecherche(), null, null, nature,
                decisionFrom, decisionTo, null, start, rowNumber, null, sort, direction,
                Collections.singleton(listeDeliberationsInput.getGroupe()));

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        List<List<Object>> rows = actes.stream().map(acte -> {
            String numberCol = acte.getNumber();
            if (!StringUtils.isEmpty(listeDeliberationsInput.getLienDetail())) {
                numberCol = "<a href='" + listeDeliberationsInput.getLienDetail() + acteService.generateMiatId(acte)
                        + "'>" + numberCol + "</a>";
            }

            ActeHistory acteHistory = acteService.getLastMetierHistory(acte.getUuid());
            String statusString = dateFormatter.format(acteHistory.getDate()) + " : "
                    + acteService.getActeHistoryDefinition(acteHistory);

            List<Object> list = Arrays.asList(acte.getNature().name(), acteService.generateMiatId(acte), numberCol,
                    dateFormatter.format(acte.getDecision()), statusString);
            return list;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();

        result.put("iTotalRecords", count);
        result.put("iTotalDisplayRecords", rows.size());
        result.put("iDisplayLength", rowNumber);
        result.put("iDisplayStart", start);
        result.put("aaData", rows);

        listeDeliberationsOutput.setJsonGetListeDeliberations(soapReturnGenerator.generateReturn("OK", result));
        return listeDeliberationsOutput;

    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getAnneeDebut")
    @ResponsePayload
    public GetAnneeDebutOutput getAnneeDebut(@RequestPayload GetAnneeDebutInput getAnneeDebutInput,
            @SoapHeader(value = "{https://stela.sictiam.fr/}authHeader") SoapHeaderElement soapHeaderElement) {

        GetAnneeDebutOutput output = objectFactory.createGetAnneeDebutOutput();

        GenericAccount account = certificateAuth(soapHeaderElement);
        if (account == null) {
            output.setJsonGetAnneeDebut(soapReturnGenerator.connectionFailedReturn());
            return output;
        }

        ActeNature nature = StringUtils.isEmpty(getAnneeDebutInput.getFiltreNature()) ? null
                : ActeNature.code(Integer.parseInt(getAnneeDebutInput.getFiltreNature()));
        Optional<Acte> acte = acteService.getFirstActeCreatedForNature(nature, getAnneeDebutInput.getGroupe(), true);

        int year = acte.isPresent() ? acte.get().getDecision().getYear() : 0;

        output.setJsonGetAnneeDebut(soapReturnGenerator.generateReturn("OK", year));
        return output;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "autoCompletion")
    @ResponsePayload
    public AutoCompletionOutput autoCompletion(@RequestPayload AutoCompletionInput autoCompletionInput,
            @SoapHeader(value = "{https://stela.sictiam.fr/}authHeader") SoapHeaderElement soapHeaderElement) {

        AutoCompletionOutput output = objectFactory.createAutoCompletionOutput();
        GenericAccount account = certificateAuth(soapHeaderElement);

        if (account == null) {
            output.setJsonAutoCompletion(soapReturnGenerator.connectionFailedReturn());
            return output;
        }
        List<Acte> actes = acteService.getActesMatchingMiatId(autoCompletionInput.getNumero());
        output.setJsonAutoCompletion(soapReturnGenerator.generateReturn("OK",
                actes.stream().map(acte -> acte.getMiatId()).collect(Collectors.toList())));
        return output;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "putActe")
    @ResponsePayload
    public PutActeOutput putActe(@RequestPayload PutActeInput putActeInput,
            @SoapHeader(value = "{https://stela.sictiam.fr/}authHeader") SoapHeaderElement soapHeaderElement)
            throws IOException {

        PutActeOutput output = objectFactory.createPutActeOutput();
        GenericAccount account = certificateAuth(soapHeaderElement);

        if (account == null) {
            output.setJsonPutActe(soapReturnGenerator.connectionFailedReturn());
            return output;
        }
        Acte acte = publishActeFromJson(putActeInput.getFileContent(), account);
        if (acte != null) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("enveloppe_id", acte.getUuid());
            hashMap.put("miat_ID", acte.getMiatId());
            output.setJsonPutActe(soapReturnGenerator.generateReturn("OK", hashMap));
        } else {
            output.setJsonPutActe(soapReturnGenerator.generateReturn("OK", "UNEXPECTED ERROR"));
        }
        return output;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "putActe_Banette")
    @ResponsePayload
    public PutActeBanetteOutput putActe_Banette(@RequestPayload PutActeBanetteInput putActeInput,
            @SoapHeader(value = "{https://stela.sictiam.fr/}authHeader") SoapHeaderElement soapHeaderElement)
            throws IOException {

        PutActeBanetteOutput output = objectFactory.createPutActeBanetteOutput();

        GenericAccount account = certificateAuth(soapHeaderElement);
        if (account == null) {
            output.setJsonPutActeBanette(soapReturnGenerator.connectionFailedReturn());
            return output;
        }
        Acte acte = publishActeFromJson(putActeInput.getFileContent(), account);
        if (acte != null) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("form_id", acte.getMiatId());
            output.setJsonPutActeBanette(soapReturnGenerator.generateReturn("OK", hashMap));
        } else {
            output.setJsonPutActeBanette(soapReturnGenerator.generateReturn("OK", "UNEXPECTED ERROR"));
        }
        return output;
    }

    public Acte publishActeFromJson(String fileContent, GenericAccount account) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = objectMapper.reader().readTree(fileContent);
        JsonNode files = node.get("fichier");
        JsonNode metadatas = node.get("informations");

        String sendGroup = metadatas.get("groupSend").asText();

        localAuthorityGranted(account, sendGroup);

        boolean granted = localAuthorityGranted(account, sendGroup);
        if (granted) {

            List<Attachment> attachments = StreamSupport.stream(files.spliterator(), false).map(file -> {
                byte[] byteArray = null;
                String name = StringUtils.stripAccents(file.get("name").asText());
                try {
                    byteArray = Base64.getDecoder().decode(file.get("base64").asText().getBytes("UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    LOGGER.error(e.getMessage());
                }
                return new Attachment(byteArray, name, byteArray.length);
            }).collect(Collectors.toList());

            Attachment mainAttachement = attachments.remove(0);
            Acte acte = new Acte(metadatas.get("numInterne").asText(),
                    LocalDate.parse(metadatas.get("dateDecision").asText()),
                    ActeNature.code(metadatas.get("natureActe").asInt()), metadatas.get("matiereActe").asText(),
                    metadatas.get("objet").asText(), metadatas.get("affichageSurSite").asBoolean(),
                    metadatas.get("affichageSurSite").asBoolean());

            acte.setActeAttachment(mainAttachement);
            acte.setAnnexes(attachments);
            acte.setCreation(LocalDateTime.now());
            LocalAuthority currentLocalAuthority = localAuthorityService.getByUuid(sendGroup);
            acte.setLocalAuthority(currentLocalAuthority);
            acte.setProfileUuid(currentLocalAuthority.getGenericProfileUuid());
            acte.setCodeLabel(
                    localAuthorityService.getCodeMatiereLabel(currentLocalAuthority.getUuid(), acte.getCode()));
            return acteService.publishActe(acte);
        }
        return null;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getAnomaliesEnveloppe")
    @ResponsePayload
    public GetAnomaliesEnveloppeOutput getAnomaliesEnveloppe(
            @RequestPayload GetAnomaliesEnveloppeInput anomaliesEnveloppeInput,
            @SoapHeader(value = "{https://stela.sictiam.fr/}authHeader") SoapHeaderElement soapHeaderElement) {

        GetAnomaliesEnveloppeOutput output = objectFactory.createGetAnomaliesEnveloppeOutput();
        GenericAccount account = certificateAuth(soapHeaderElement);
        if (account == null) {
            output.setJsonGetAnomaliesEnveloppe(soapReturnGenerator.connectionFailedReturn());
            return output;
        }
        Acte acte = acteService.getByUuid(anomaliesEnveloppeInput.getEnveloppeId());
        Optional<ActeHistory> acteHistory = acteService.findFirstActeHistory(acte, StatusType.NACK_RECEIVED);
        if (acteHistory.isPresent()) {
            output.setJsonGetAnomaliesEnveloppe(
                    soapReturnGenerator.generateReturn("OK", acteHistory.get().getMessage()));
        } else {
            output.setJsonGetAnomaliesEnveloppe(soapReturnGenerator.generateReturn("OK", "OK"));

        }
        return output;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "annulationActe")
    @ResponsePayload
    public AnnulationActeOutput annulationActe(@RequestPayload AnnulationActeInput annulationActeInput,
            @SoapHeader(value = "{https://stela.sictiam.fr/}authHeader") SoapHeaderElement soapHeaderElement) {

        AnnulationActeOutput output = objectFactory.createAnnulationActeOutput();
        GenericAccount account = certificateAuth(soapHeaderElement);
        if (account == null) {
            output.setJsonAnnulationActe(soapReturnGenerator.connectionFailedReturn());
            return output;
        }
        try {
            Acte acte = acteService.findByIdActe(annulationActeInput.getMiatID());
            acteService.cancel(acte.getUuid());
            output.setJsonAnnulationActe(
                    soapReturnGenerator.generateReturn("OK", "_MIAT_ANNULATION_ASKED " + acte.getUuid()));

        } catch (ActeNotFoundException e) {
            output.setJsonAnnulationActe(soapReturnGenerator.generateReturn("NOK", "_ERROR_ACTE_INEXISTANT"));
        } catch (CancelForbiddenException e) {
            output.setJsonAnnulationActe(soapReturnGenerator.generateReturn("NOK", "_ERROR_ACTE_NOT_CANCELABLE"));
        }

        return output;
    }

}
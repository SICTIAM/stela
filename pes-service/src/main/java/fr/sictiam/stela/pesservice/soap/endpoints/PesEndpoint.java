package fr.sictiam.stela.pesservice.soap.endpoints;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sictiam.stela.pesservice.model.Attachment;
import fr.sictiam.stela.pesservice.model.PesAller;
import fr.sictiam.stela.pesservice.model.PesHistory;
import fr.sictiam.stela.pesservice.model.PesRetour;
import fr.sictiam.stela.pesservice.model.StatusType;
import fr.sictiam.stela.pesservice.model.ui.GenericAccount;
import fr.sictiam.stela.pesservice.service.ExternalRestService;
import fr.sictiam.stela.pesservice.service.LocalAuthorityService;
import fr.sictiam.stela.pesservice.service.PesAllerService;
import fr.sictiam.stela.pesservice.service.PesRetourService;
import fr.sictiam.stela.pesservice.soap.model.*;
import fr.sictiam.stela.pesservice.validation.ValidationUtil;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.ObjectError;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.server.endpoint.annotation.SoapHeader;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBElement;
import javax.xml.transform.dom.DOMResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Endpoint
public class PesEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(PesEndpoint.class);

    private static final String NAMESPACE_URI = "urn:myInputNamespace";

    private final PesAllerService pesAllerService;
    private final PesRetourService pesRetourService;
    private final LocalAuthorityService localAuthorityService;
    private final SoapReturnGenerator soapReturnGenerator;
    private final ExternalRestService externalRestService;
    private ObjectFactory objectFactory;

    public PesEndpoint(PesAllerService pesAllerService, LocalAuthorityService localAuthorityService,
            SoapReturnGenerator soapReturnGenerator, ExternalRestService externalRestService,
            PesRetourService pesRetourService) {
        this.pesAllerService = pesAllerService;
        this.localAuthorityService = localAuthorityService;
        this.soapReturnGenerator = soapReturnGenerator;
        this.externalRestService = externalRestService;
        this.pesRetourService = pesRetourService;
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
                .anyMatch(localAuthority -> localAuthority.getActivatedModules().contains("PES")
                        && localAuthority.getUuid().equals(localAuthorityUuid));
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "connexionSTELA")
    public @ResponsePayload JAXBElement<ConnexionSTELAOutput> connexionSTELA(
            @RequestPayload ConnexionSTELAInput connexionSTELAInput,
            @SoapHeader(value = "{https://stela.sictiam.fr/}authHeader") SoapHeaderElement soapHeaderElement) {
        String typeRetour = connexionSTELAInput.getTypeRetour();
        if (StringUtils.isEmpty(connexionSTELAInput.getTypeRetour())) {
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

        return objectFactory.createSetConnexionSTELA(returnObject);
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "connexionSTELA2")
    public @ResponsePayload ConnexionSTELAOutput2 connexionSTELA2(
            @RequestPayload ConnexionSTELAInput2 getConnexionSTELA2,
            @SoapHeader(value = "{https://stela.sictiam.fr/}authHeader") SoapHeaderElement soapHeaderElement) {

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

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getResultatFormHelios")
    public @ResponsePayload GetResultatFormHeliosOutput getResultatFormHelios(
            @RequestPayload GetResultatFormHeliosInput getResultatFormHeliosInput,
            @SoapHeader(value = "{https://stela.sictiam.fr/}authHeader") SoapHeaderElement soapHeaderElement) {

        GetResultatFormHeliosOutput returnObject = objectFactory.createGetResultatFormHeliosOutput();

        GenericAccount genericAccount = emailOrCertificateAuth(soapHeaderElement, getResultatFormHeliosInput.getEmail(),
                getResultatFormHeliosInput.getPassword());
        if (genericAccount == null) {
            returnObject.setJsonReturnResultatFormHelios(soapReturnGenerator.connectionFailedReturn());
            return returnObject;
        }

        List<String> granted = genericAccount.getLocalAuthorities().stream()
                .filter(localAuthority -> localAuthority.getActivatedModules().contains("PES"))
                .map(localAuthority -> localAuthority.getUuid()).collect(Collectors.toList());

        returnObject.setJsonReturnResultatFormHelios(soapReturnGenerator.generateReturn("OK", granted));

        return returnObject;

    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getPESAller")
    public @ResponsePayload GetPESAllerOutput getPESAller(@RequestPayload GetPESAllerInput getPESAllerInput,
            @SoapHeader(value = "{https://stela.sictiam.fr/}authHeader") SoapHeaderElement soapHeaderElement) {

        GetPESAllerOutput returnObject = objectFactory.createGetPESAllerOutput();

        GenericAccount genericAccount = emailOrCertificateAuth(soapHeaderElement, getPESAllerInput.getEmail(),
                getPESAllerInput.getPassword());
        if (genericAccount == null) {
            returnObject.setJsonGetPESAller(soapReturnGenerator.connectionFailedReturn());
            return returnObject;
        }
        PesAller pesAller = pesAllerService.getByUuid(getPESAllerInput.getEnveloppeId());
        List<PesHistory> fileHistories = pesAllerService.getPesHistoryByTypes(getPESAllerInput.getEnveloppeId(),
                Arrays.asList(StatusType.ACK_RECEIVED, StatusType.NACK_RECEIVED));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);

        try {
            addEntry(pesAller.getAttachment().getFilename(), pesAller.getAttachment().getFile(), taos);

            fileHistories.forEach(pesHistory -> {
                try {
                    addEntry(pesHistory.getFileName(), pesHistory.getFile(), taos);
                } catch (IOException e) {
                    LOGGER.error(e.getMessage());
                }
            });
            taos.close();
            baos.close();

            ByteArrayOutputStream archive = compress(baos);
            String archiveName = pesAller.getAttachment().getFilename() + ".tar.gz";
            String archiveBase64 = Base64.encode(archive.toByteArray());
            Map<String, String> returnMap = new HashMap<>();
            returnMap.put("chaine_archive", archiveBase64);
            returnMap.put("filename", archiveName);

            returnObject.setJsonGetPESAller(soapReturnGenerator.generateReturn("OK", returnMap));

        } catch (IOException e) {
            returnObject.setJsonGetPESAller(soapReturnGenerator.generateReturn("NOK", "UnknownError"));
        }

        return returnObject;

    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getACKAller")
    public @ResponsePayload GetACKAllerOutput getACKAller(@RequestPayload GetACKAllerInput getACKAllerInput,
            @SoapHeader(value = "{https://stela.sictiam.fr/}authHeader") SoapHeaderElement soapHeaderElement) {

        GetACKAllerOutput returnObject = objectFactory.createGetACKAllerOutput();

        GenericAccount genericAccount = emailOrCertificateAuth(soapHeaderElement, getACKAllerInput.getEmail(),
                getACKAllerInput.getPassword());
        if (genericAccount == null) {
            returnObject.setJsonGetACKAller(soapReturnGenerator.connectionFailedReturn());
            return returnObject;
        }

        List<PesHistory> fileHistories = pesAllerService.getPesHistoryByTypes(getACKAllerInput.getEnveloppeId(),
                Arrays.asList(StatusType.ACK_RECEIVED, StatusType.NACK_RECEIVED));

        Optional<PesHistory> peshistory = fileHistories.stream().findFirst();

        if (peshistory.isPresent()) {
            Map<String, String> returnMap = new HashMap<>();
            returnMap.put("chaine_archive", Base64.encode(peshistory.get().getFile()));
            returnMap.put("filename", peshistory.get().getFileName());

            returnObject.setJsonGetACKAller(soapReturnGenerator.generateReturn("OK", returnMap));
        }

        return returnObject;

    }

    private void addEntry(String entryName, byte[] content, TarArchiveOutputStream taos) throws IOException {
        File file = new File(entryName);
        FileCopyUtils.copy(content, file);
        ArchiveEntry archiveEntry = new TarArchiveEntry(file, entryName);
        taos.putArchiveEntry(archiveEntry);
        IOUtils.copy(new FileInputStream(file), taos);
        taos.closeArchiveEntry();
        file.delete();
    }

    private ByteArrayOutputStream compress(ByteArrayOutputStream baos) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        GzipCompressorOutputStream gcos = new GzipCompressorOutputStream(baos2);
        final byte[] buffer = new byte[2048];
        int n;
        while (-1 != (n = bais.read(buffer))) {
            gcos.write(buffer, 0, n);
        }
        gcos.close();
        bais.close();
        return baos2;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getDetailsPESAller")
    public @ResponsePayload GetDetailsPESAllerOutput getDetailsPESAller(
            @RequestPayload GetDetailsPESAllerInput detailInput,
            @SoapHeader(value = "{https://stela.sictiam.fr/}authHeader") SoapHeaderElement soapHeaderElement) {

        GetDetailsPESAllerOutput returnObject = objectFactory.createGetDetailsPESAllerOutput();

        GenericAccount genericAccount = emailOrCertificateAuth(soapHeaderElement, detailInput.getEmail(),
                detailInput.getPassword());
        if (genericAccount == null) {
            returnObject.setJsonGetDetailsPESAller(soapReturnGenerator.connectionFailedReturn());
            return returnObject;
        }

        PesAller pesAller = pesAllerService.getByUuid(detailInput.getEnveloppeId());
        List<PesHistory> fileHistories = pesAllerService.getPesHistoryByTypes(detailInput.getEnveloppeId(),
                Arrays.asList(StatusType.ACK_RECEIVED, StatusType.NACK_RECEIVED));

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String userName = "";
        try {
            JsonNode profile = externalRestService.getProfile(pesAller.getProfileUuid());
            userName = profile.get("agent").get("email").asText();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        Optional<PesHistory> peshistory = fileHistories.stream().findFirst();
        Map<String, String> returnMap = new HashMap<>();
        returnMap.put("objet", pesAller.getObjet());
        returnMap.put("user_name", userName);
        returnMap.put("nomDocument", pesAller.getAttachment().getFilename());
        returnMap.put("dateDepot", dateFormatter.format(pesAller.getCreation()));

        if (peshistory.isPresent()) {
            if (peshistory.get().getStatus().equals(StatusType.ACK_RECEIVED)) {
                returnMap.put("dateAR", dateFormatter.format(peshistory.get().getDate()));
            } else if (peshistory.get().getStatus().equals(StatusType.NACK_RECEIVED)) {
                returnMap.put("dateAnomalie", dateFormatter.format(peshistory.get().getDate()));
                returnMap.put("motifAnomalie", peshistory.get().getMessage());
            }
        }
        returnObject.setJsonGetDetailsPESAller(soapReturnGenerator.generateReturn("OK", returnMap));

        return returnObject;

    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getPESRetour")
    public @ResponsePayload GetPESRetourOutput getPESRetour(@RequestPayload GetPESRetourInput pesRetourInput,
            @SoapHeader(value = "{https://stela.sictiam.fr/}authHeader") SoapHeaderElement soapHeaderElement) {

        GetPESRetourOutput returnObject = objectFactory.createGetPESRetourOutput();

        GenericAccount genericAccount = certificateAuth(soapHeaderElement);
        if (genericAccount == null) {
            returnObject.setJsonGetPESRetour(soapReturnGenerator.connectionFailedReturn());
            return returnObject;
        }

        List<PesRetour> pesRetours = pesRetourService
                .getUncollectedLocalAuthrotityPesRetours(StringUtils.substring(pesRetourInput.getIcColl(), 0, 9));

        List<Map<String, String>> outputList = pesRetours.stream().map(pesRetour -> {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);
                addEntry(pesRetour.getAttachment().getFilename(), pesRetour.getAttachment().getFile(), taos);

                taos.close();
                baos.close();

                ByteArrayOutputStream archive = compress(baos);
                String archiveName = pesRetour.getAttachment().getFilename() + ".tar.gz";
                String archiveBase64 = Base64.encode(archive.toByteArray());
                Map<String, String> returnMap = new HashMap<>();
                returnMap.put("chaine_archive", archiveBase64);
                returnMap.put("filename", archiveName);
                return returnMap;

            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
            return null;
        }).filter(pesRetourMap -> pesRetourMap != null).collect(Collectors.toList());

        returnObject.setJsonGetPESRetour(soapReturnGenerator.generateReturn("OK", outputList));

        return returnObject;

    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "sendACKPESRetour")
    public @ResponsePayload SendACKPESRetourOutput sendACKPESRetour(
            @RequestPayload SendACKPESRetourInput sendACKPESRetourInput,
            @SoapHeader(value = "{https://stela.sictiam.fr/}authHeader") SoapHeaderElement soapHeaderElement) {

        SendACKPESRetourOutput returnObject = objectFactory.createSendACKPESRetourOutput();

        GenericAccount genericAccount = certificateAuth(soapHeaderElement);
        if (genericAccount == null) {
            returnObject.setJsonSendACKPESRetour(soapReturnGenerator.connectionFailedReturn());
            return returnObject;
        }
        PesRetour pesRetour = pesRetourService.collect(sendACKPESRetourInput.getFileName());

        if (pesRetour != null) {
            returnObject.setJsonSendACKPESRetour(soapReturnGenerator.generateReturn("OK", "ACK pris en compte"));
        } else {
            returnObject
                    .setJsonSendACKPESRetour(soapReturnGenerator.generateReturn("OK", "_ERROR_DOCUMENT_INEXISTANT"));
        }

        return returnObject;

    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getDetailsPESRetour")
    public @ResponsePayload GetDetailsPESRetourOutput getDetailsPESRetour(
            @RequestPayload GetDetailsPESRetourInput detailInput,
            @SoapHeader(value = "{https://stela.sictiam.fr/}authHeader") SoapHeaderElement soapHeaderElement) {

        GetDetailsPESRetourOutput returnObject = objectFactory.createGetDetailsPESRetourOutput();
        GenericAccount genericAccount = certificateAuth(soapHeaderElement);
        if (genericAccount == null) {
            returnObject.setJsonGetDetailsPESRetour(soapReturnGenerator.connectionFailedReturn());
            return returnObject;
        }
        PesRetour pesRetour = pesRetourService.getByUuid(detailInput.getEnveloppeId());

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter titleFormatter = DateTimeFormatter.ofPattern("'Retour HELIOS - 'dd/MM/yyyy hh:mm:ss");

        Map<String, String> returnMap = new HashMap<>();
        returnMap.put("objet", titleFormatter.format(pesRetour.getCreation()));
        returnMap.put("nomFichier", pesRetour.getAttachment().getFilename() + ".tar.gz");
        returnMap.put("metier_list_enveloppe_retour", pesRetour.getAttachment().getFilename());
        returnMap.put("date_enveloppe_retour", dateFormatter.format(pesRetour.getCreation()));

        returnObject.setJsonGetDetailsPESRetour(soapReturnGenerator.generateReturn("OK", returnMap));

        return returnObject;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "putPESAller")
    public @ResponsePayload PutPESAllerOutput putPESAller(@RequestPayload PutPESAllerInput putPESAllerInput,
            @SoapHeader(value = "{https://stela.sictiam.fr/}authHeader") SoapHeaderElement soapHeaderElement) {

        PutPESAllerOutput returnObject = objectFactory.createPutPESAllerOutput();

        GenericAccount genericAccount = emailOrCertificateAuth(soapHeaderElement, putPESAllerInput.getEmail(),
                putPESAllerInput.getPassword());
        if (genericAccount == null) {
            returnObject.setRetour(soapReturnGenerator.connectionFailedReturn());
            return returnObject;
        }

        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode node = mapper.reader().readTree(putPESAllerInput.getFileContent());
            JsonNode files = node.get("fichier");
            JsonNode metadatas = node.get("informations");

            String sendGroup = metadatas.get("groupSend").asText();
            // JsonNode profiles =
            // externalRestService.getAgentProfiles(metadatas.get("uid").asText());

            boolean granted = localAuthorityGranted(genericAccount, sendGroup);

            if (granted) {
                List<Attachment> attachments = StreamSupport.stream(files.spliterator(), false).map(file -> {
                    byte[] byteArray = null;
                    String name = StringUtils.stripAccents(file.get("name").asText());
                    try {
                        byteArray = Base64.decode(file.get("base64").asText().getBytes("UTF-8"));
                    } catch (UnsupportedEncodingException | Base64DecodingException e) {
                        LOGGER.error(e.getMessage());
                    }
                    return new Attachment(byteArray, name, byteArray.length);
                }).collect(Collectors.toList());

                Attachment mainAttachement = attachments.remove(0);
                if (pesAllerService.checkVirus(mainAttachement.getFile())) {
                    returnObject.setRetour(soapReturnGenerator.generateReturn("NOK", "VIRUS_FOUND"));
                    return returnObject;
                }
                PesAller pesAller = new PesAller();
                List<ObjectError> errors = ValidationUtil.validatePes(pesAller);
                if (!errors.isEmpty()) {
                    returnObject.setRetour(soapReturnGenerator.generateReturn("NOK", "INVALID_DATAS"));
                    return returnObject;
                }
                pesAller.setAttachment(mainAttachement);
                pesAller.setCreation(LocalDateTime.now());
                pesAller = pesAllerService.populateFromByte(pesAller, mainAttachement.getFile());
                if (pesAllerService.getByFileName(pesAller.getFileName()).isPresent()) {
                    returnObject.setRetour(soapReturnGenerator.generateReturn("NOK", "DUPLICATE_FILE"));
                    return returnObject;
                }

                pesAllerService.create(localAuthorityService.getByUuid(sendGroup).getGenericProfileUuid(), sendGroup,
                        pesAller);

                returnObject.setRetour(soapReturnGenerator.generateReturn("OK", "_HELIOS_OK_INSERT"));
            }

        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            returnObject.setRetour(soapReturnGenerator.generateReturn("NOK", "UNKNOW_ERROR"));
        }

        return returnObject;
    }

}
package fr.sictiam.stela.acteservice.soap.endpoints;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.model.Attachment;
import fr.sictiam.stela.acteservice.model.LocalAuthority;
import fr.sictiam.stela.acteservice.model.MaterialCode;
import fr.sictiam.stela.acteservice.service.ActeService;
import fr.sictiam.stela.acteservice.service.ExternalRestService;
import fr.sictiam.stela.acteservice.service.LocalAuthorityService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.ws.test.server.MockWebServiceClient;
import org.springframework.xml.transform.StringSource;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static fr.sictiam.stela.acteservice.TestDataGenerator.acte;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.ws.test.server.RequestCreators.withPayload;
import static org.springframework.ws.test.server.ResponseMatchers.payload;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = PaullEndpoint.class)
@ActiveProfiles("test")
public class PaullEndointTest {

    @Autowired
    private ApplicationContext applicationContext;

    private MockWebServiceClient mockClient;

    @SpyBean
    private PaullEndpoint paullEndpoint;

    @MockBean
    private ActeService acteService;
    @MockBean
    private LocalAuthorityService localAuthorityService;
    @MockBean
    private SoapReturnGenerator soapReturnGenerator;
    @MockBean
    private ExternalRestService externalRestService;

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Before
    public void createClient() {
        mockClient = MockWebServiceClient.createClient(applicationContext);
    }

    @Test
    public void shouldReturnAnErrorIfPaullTokenIsInvalid() {
        String getClassificationActeRequest =
            "<getClassificationActeRequest xmlns='http://www.processmaker.com'>" +
                "<sessionId>sessionId</sessionId>" + "" +
            "</getClassificationActeRequest>";

        String getClassificationActeResponse =
            "<getClassificationActeResponse xmlns='http://www.processmaker.com'>" +
                "<statusCode>NOK</statusCode>" +
                "<retour><message>SESSION_INVALID_OR_EXPIRED</message></retour>" +
            "</getClassificationActeResponse>";

        given(paullEndpoint.getToken(any()))
                .willReturn(null);

        mockClient.sendRequest(withPayload(new StringSource(getClassificationActeRequest)))
                .andExpect(payload(new StringSource(getClassificationActeResponse)));
    }

    @Test
    public void shouldReturnAnErrorIfLocalAuthorityIsNotGranted() {
        String getClassificationActeRequest =
            "<getClassificationActeRequest xmlns='http://www.processmaker.com'>" +
                "<sessionId>sessionId</sessionId>" + "" +
            "</getClassificationActeRequest>";

        String getClassificationActeResponse =
            "<getClassificationActeResponse xmlns='http://www.processmaker.com'>" +
                "<statusCode>NOK</statusCode>" +
                "<retour><message>LOCALAUTHORITY_NOT_GRANTED</message></retour>" +
            "</getClassificationActeResponse>";

        given(paullEndpoint.getToken(any()))
                .willReturn(new PaullSoapToken());
        given(localAuthorityService.localAuthorityGranted(any(), any()))
                .willReturn(false);

        mockClient.sendRequest(withPayload(new StringSource(getClassificationActeRequest)))
                .andExpect(payload(new StringSource(getClassificationActeResponse)));
    }

    @Test
    public void shouldReturnAnErrorIfNoLocalAuthorityForPaullToken() {
        String getClassificationActeRequest =
            "<getClassificationActeRequest xmlns='http://www.processmaker.com'>" +
                "<sessionId>sessionId</sessionId>" + "" +
            "</getClassificationActeRequest>";

        String getClassificationActeResponse =
            "<getClassificationActeResponse xmlns='http://www.processmaker.com'>" +
                "<statusCode>NOK</statusCode>" +
                "<retour><message>LOCALAUTHORITY_NOT_FOUND_FOR_SIREN</message></retour>" +
            "</getClassificationActeResponse>";

        given(paullEndpoint.getToken(any()))
                .willReturn(new PaullSoapToken());
        given(localAuthorityService.localAuthorityGranted(any(), any()))
                .willReturn(true);
        given(localAuthorityService.getBySiren(any()))
                .willReturn(Optional.empty());

        mockClient.sendRequest(withPayload(new StringSource(getClassificationActeRequest)))
                .andExpect(payload(new StringSource(getClassificationActeResponse)));
    }

    @Test
    public void shouldReturnAnErrorIfNoNomenclaureForLocalAuthority() {
        String getClassificationActeRequest =
            "<getClassificationActeRequest xmlns='http://www.processmaker.com'>" +
                "<sessionId>sessionId</sessionId>" + "" +
            "</getClassificationActeRequest>";

        String getClassificationActeResponse =
            "<getClassificationActeResponse xmlns='http://www.processmaker.com'>" +
                "<statusCode>NOK</statusCode>" +
                "<retour><message>NO_NOMENCLATURE_FOR_LOCALAUTHORITY</message></retour>" +
            "</getClassificationActeResponse>";

        given(paullEndpoint.getToken(any()))
                .willReturn(new PaullSoapToken());
        given(localAuthorityService.localAuthorityGranted(any(), any()))
                .willReturn(true);
        LocalAuthority localAuthority = new LocalAuthority("uuid", "name", "siren", false);
        given(localAuthorityService.getBySiren(any()))
                .willReturn(Optional.of(localAuthority));

        mockClient.sendRequest(withPayload(new StringSource(getClassificationActeRequest)))
                .andExpect(payload(new StringSource(getClassificationActeResponse)));
    }

    @Test
    public void shouldReturnClassification() throws IOException {
        String getClassificationActeRequest =
            "<getClassificationActeRequest xmlns='http://www.processmaker.com'>" +
                "<sessionId>sessionId</sessionId>" + "" +
            "</getClassificationActeRequest>";

        String getClassificationActeResponse =
            "<getClassificationActeResponse xmlns='http://www.processmaker.com'>" +
                "<statusCode>OK</statusCode>" +
                "<retour>" +
                    "<message>OK</message>" +
                    "<codeMatiere><cle>1-1</cle><valeur>Marchés publics</valeur></codeMatiere>" +
                    "<codeMatiere><cle>1-2</cle><valeur>Délégation de service public</valeur></codeMatiere>" +
                    "<natureActes><cle>01</cle><valeur>DELIBERATIONS</valeur></natureActes>" +
                    "<natureActes><cle>02</cle><valeur>ARRETES_REGLEMENTAIRES</valeur></natureActes>" +
                    "<natureActes><cle>03</cle><valeur>ARRETES_INDIVIDUELS</valeur></natureActes>" +
                    "<natureActes><cle>04</cle><valeur>CONTRATS_ET_CONVENTIONS</valeur></natureActes>" +
                    "<natureActes><cle>05</cle><valeur>DOCUMENTS_BUDGETAIRES_ET_FINANCIERS</valeur></natureActes>" +
                    "<natureActes><cle>06</cle><valeur>AUTRES</valeur></natureActes>" +
                    "<collectiviteDateClassification>01/01/2001</collectiviteDateClassification>" +
                "</retour>" +
            "</getClassificationActeResponse>";

        given(paullEndpoint.getToken(any()))
                .willReturn(new PaullSoapToken());
        given(localAuthorityService.localAuthorityGranted(any(), any()))
                .willReturn(true);
        given(localAuthorityService.getBySiren(any()))
                .willReturn(localAuthority());

        mockClient.sendRequest(withPayload(new StringSource(getClassificationActeRequest)))
                .andExpect(payload(new StringSource(getClassificationActeResponse)));
    }

    @Test
    public void shouldReturnANewActe() throws IOException {
        String depotActeRequest =
            "<depotActeRequest xmlns='http://www.processmaker.com'>" +
                "<sessionId>sessionId</sessionId>" +
                "<infosActe>" +
                    "<dateDecision>25/02/2019</dateDecision>" +
                    "<numInterne>20190226001</numInterne>" +
                    "<natureActe>3</natureActe>" +
                    "<matiereActe>2-1-1-0-0</matiereActe>" +
                    "<objet>test stela 3</objet>" +
                    "<precedent_acte_id></precedent_acte_id>" +
                    "<name>SICTIAM-20190226011</name>" +
                    "<desc></desc>" +
                    "<validation>26/02/2019</validation>" +
                    "<email>stela3@sictiam.fr</email>" +
                "</infosActe>" +
                "<fichiers>" +
                    "<filename>ARRÊTÉ MUNICIPAL Sictiamville.pdf</filename>" +
                    "<base64></base64>" +
                "</fichiers>" +
            "</depotActeRequest>";

        String depotActeResponse =
            "<depotActeResponse xmlns='http://www.processmaker.com'>" +
                "<statusCode>OK</statusCode>" +
                "<retour>" +
                    "<message>OK</message>" +
                    "<idActe>uuid</idActe>" +
                "</retour>" +
            "</depotActeResponse>";

        given(paullEndpoint.getToken(any()))
                .willReturn(new PaullSoapToken());
        given(localAuthorityService.localAuthorityGranted(any(), any()))
                .willReturn(true);
        given(localAuthorityService.getBySiren(any()))
                .willReturn(localAuthority());
        given(acteService.publishActe(any()))
                .willReturn(acte());

        mockClient.sendRequest(withPayload(new StringSource(depotActeRequest)))
                .andExpect(payload(new StringSource(depotActeResponse)));
    }

    @Test
    public void shouldReturnAnActeDetails() throws IOException {
        String getDetailsActeRequest =
            "<getDetailsActeRequest xmlns='http://www.processmaker.com'>" +
                "<sessionId>sessiondId</sessionId>" + "" +
                "<idActe>uuid</idActe>" + "" +
            "</getDetailsActeRequest>";

        String getDetailsActeResponse =
            "<getDetailsActeResponse xmlns='http://www.processmaker.com'>" +
                "<statusCode>OK</statusCode>" + "" +
                "<retour>" +
                    "<message>OK</message>" +
                    "<numActe>number</numActe>" +
                    "<objet>Objet</objet>" +
                    "<userName>agent@sictiam.fr</userName>" +
                    "<natureActe>ARRETES_INDIVIDUELS</natureActe>" +
                    "<nomDocument>attachment.pdf</nomDocument>" +
                    "<annexesList/>" +
                    "<dateDecision>" + dateFormatter.format(LocalDateTime.now()) + "</dateDecision>" +
                    "<dateDepotActe/>" +
                    "<dateAR/>" +
                    "<dateARAnnul/>" +
                    "<anomalies/>" +
                "</retour>" +
            "</getDetailsActeResponse>";

        given(paullEndpoint.getToken(any()))
                .willReturn(new PaullSoapToken());
        given(localAuthorityService.localAuthorityGranted(any(), any()))
                .willReturn(true);
        given(localAuthorityService.getBySiren(any()))
                .willReturn(localAuthority());
        Acte acte = acte();
        acte.setActeAttachment(new Attachment(null, "attachment.pdf", 10000));
        given(acteService.getByUuid(any()))
                .willReturn(acte);
        given(externalRestService.getProfile(any()))
                .willReturn(profileNode());

        mockClient.sendRequest(withPayload(new StringSource(getDetailsActeRequest)))
                .andExpect(payload(new StringSource(getDetailsActeResponse)));
    }

    private Optional<LocalAuthority> localAuthority() throws IOException {
        LocalAuthority localAuthority = new LocalAuthority("639fd48c-93b9-4569-a414-3b372c71e0a1",
                "SICTIAM TEST","214400152", "slug-test");
        localAuthority.setMaterialCodes(materialCodeList(localAuthority));
        return Optional.of(localAuthority);
    }

    private List<MaterialCode> materialCodeList(LocalAuthority localAuthority) {
        MaterialCode materialCode = new MaterialCode("1-1", "Marchés publics", localAuthority);
        MaterialCode materialCode2 = new MaterialCode("1-2", "Délégation de service public", localAuthority);
        return Arrays.asList(materialCode, materialCode2);
    }

    private JsonNode profileNode() throws IOException {
        String profile = "{ \"agent\": { \"email\": \"agent@sictiam.fr\" } }";
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(profile);
    }
}

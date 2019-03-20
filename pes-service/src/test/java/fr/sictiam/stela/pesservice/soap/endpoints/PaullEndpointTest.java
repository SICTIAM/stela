package fr.sictiam.stela.pesservice.soap.endpoints;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sictiam.stela.pesservice.model.LocalAuthority;
import fr.sictiam.stela.pesservice.model.PesAller;
import fr.sictiam.stela.pesservice.model.PesHistory;
import fr.sictiam.stela.pesservice.model.StatusType;
import fr.sictiam.stela.pesservice.model.sesile.Action;
import fr.sictiam.stela.pesservice.model.sesile.Classeur;
import fr.sictiam.stela.pesservice.model.sesile.ClasseurStatus;
import fr.sictiam.stela.pesservice.service.*;
import fr.sictiam.stela.pesservice.soap.model.paull.PaullSoapToken;
import io.vavr.control.Either;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.ws.test.server.RequestCreators.*;
import static org.springframework.ws.test.server.ResponseMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = PaullEndpoint.class)
@ActiveProfiles("test")
public class PaullEndpointTest {

    @Autowired
    private ApplicationContext applicationContext;

    private MockWebServiceClient mockClient;

    @SpyBean
    private PaullEndpoint paullEndpoint;

    @MockBean
    private PesAllerService pesAllerService;
    @MockBean
    private PesRetourService pesRetourService;
    @MockBean
    private LocalAuthorityService localAuthorityService;
    @MockBean
    private ExternalRestService externalRestService;
    @MockBean
    private SesileService sesileService;

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Before
    public void createClient() {
        mockClient = MockWebServiceClient.createClient(applicationContext);
    }

    @Test
    public void getDetailsPesAllerNoToken() {
        String detailsPesAllerRequest =
            "<getDetailsPESAllerRequest xmlns='http://www.processmaker.com'>" +
                "<sessionId>sessionid</sessionId>" +
                "<IdPesAller>aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee</IdPesAller>" +
            "</getDetailsPESAllerRequest>";

        String detailsPesAllerResponseNoToken =
            "<ns2:getDetailsPESAllerResponse xmlns:ns2='http://www.processmaker.com'>" +
                "<ns2:statusCode>NOK</ns2:statusCode>" +
                "<ns2:retour><ns2:message>SESSION_INVALID_OR_EXPIRED</ns2:message></ns2:retour>" +
            "</ns2:getDetailsPESAllerResponse>";

        given(paullEndpoint.getToken(any()))
                .willReturn(null);

        mockClient.sendRequest(withPayload(new StringSource(detailsPesAllerRequest)))
            .andExpect(payload(new StringSource(detailsPesAllerResponseNoToken)));
    }

    @Test
    public void getDetailsPesAllerNoLocalAuthority() {
        String detailsPesAllerRequest =
            "<getDetailsPESAllerRequest xmlns='http://www.processmaker.com'>" +
                "<sessionId>sessionid</sessionId>" +
                "<IdPesAller>aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee</IdPesAller>" +
            "</getDetailsPESAllerRequest>";

        String detailsPesAllerResponseNoLocalAuthority =
            "<ns2:getDetailsPESAllerResponse xmlns:ns2='http://www.processmaker.com'>" +
                "<ns2:statusCode>NOK</ns2:statusCode>" +
                "<ns2:retour><ns2:message>LOCALAUTHORITY_NOT_GRANTED</ns2:message></ns2:retour>" +
            "</ns2:getDetailsPESAllerResponse>";

        given(paullEndpoint.getToken(any()))
                .willReturn(new PaullSoapToken());
        given(localAuthorityService.localAuthorityGranted(any(), any()))
                .willReturn(false);

        mockClient.sendRequest(withPayload(new StringSource(detailsPesAllerRequest)))
                .andExpect(payload(new StringSource(detailsPesAllerResponseNoLocalAuthority)));
    }

    @Test
    public void getDetailsPesAller() throws IOException, ParseException {
        String detailsPesAllerRequest =
            "<getDetailsPESAllerRequest xmlns='http://www.processmaker.com'>" +
                "<sessionId>sessionid</sessionId>" +
                "<IdPesAller>aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee</IdPesAller>" +
            "</getDetailsPESAllerRequest>";

        String detailsPesAllerResponse =
            "<ns2:getDetailsPESAllerResponse xmlns:ns2=\"http://www.processmaker.com\">" +
                "<ns2:statusCode>OK</ns2:statusCode>" +
                "<ns2:retour>" +
                    "<ns2:message>SUCCESS</ns2:message>" +
                    "<ns2:PESPJ>0</ns2:PESPJ>" +
                    "<ns2:objet>objet</ns2:objet>" +
                    "<ns2:userName>agent@sictiam.fr</ns2:userName>" +
                    "<ns2:nomDocument>pes.xml</ns2:nomDocument>" +
                    "<ns2:dateDepot>" + dateFormatter.format(LocalDateTime.now()) + "</ns2:dateDepot>" +
                    "<ns2:dateAR>" + dateFormatter.format(LocalDateTime.now()) + "</ns2:dateAR>" +
                    "<ns2:dateAnomalie/><ns2:motifAnomalie/>" +
                    "<ns2:motifPlusAnomalie/>" +
                    "<ns2:userNameBannette/>" +
                    "<ns2:dateDepotBannette/>" +
                    "<ns2:statutBannette/>" +
                    "<ns2:etatclasseur>2</ns2:etatclasseur>" +
                    "<ns2:acteurCourant/>" +
                    "<ns2:circuitClasseur/>" +
                    "<ns2:actionsClasseur>" +
                        "<ns2:nomActeur>agent-pes</ns2:nomActeur>" +
                        "<ns2:dateAction>20/03/2019</ns2:dateAction>" +
                        "<ns2:libelleAction>signature</ns2:libelleAction>" +
                    "</ns2:actionsClasseur>" +
                "</ns2:retour>" +
            "</ns2:getDetailsPESAllerResponse>";

        given(paullEndpoint.getToken(any()))
                .willReturn(new PaullSoapToken());
        given(localAuthorityService.getBySiren(any()))
                .willReturn(localAuthority());
        given(localAuthorityService.localAuthorityGranted(any(), any()))
                .willReturn(true);
        given(pesAllerService.getByUuid(any()))
                .willReturn(pesAller());
        given(sesileService.getClasseur(any(), any()))
                .willReturn(Either.right(classeur()));
        given(externalRestService.getProfile(any()))
                .willReturn(profileNode());
        given(pesAllerService.getPesHistoryByTypes(any(), any()))
                .willReturn(Collections.singletonList(pesHistory()));

        mockClient.sendRequest(withPayload(new StringSource(detailsPesAllerRequest)))
                .andExpect(payload(new StringSource(detailsPesAllerResponse)));
    }

    private Optional<LocalAuthority> localAuthority() {
        return Optional.of(new LocalAuthority("639fd48c-93b9-4569-a414-3b372c71e0a1", "SICTIAM TEST",
                "214400152", true));
    }

    private PesAller pesAller() {
        PesAller pesAller = new PesAller(LocalDateTime.now(), "objet", null, null, null,
                "profile-uuid", "comment", "PDF", "code", "postid",
                "budcode", "pes.xml", false, true, 1234, false);
        pesAller.setLastHistoryStatus(StatusType.ACK_RECEIVED);
        return pesAller;
    }

    public PesHistory pesHistory() {
        return new PesHistory("pes-history-uuid", StatusType.ACK_RECEIVED, LocalDateTime.now());
    }

    private Classeur classeur() throws ParseException {
        Classeur classeur = new Classeur();
        classeur.setStatus(ClasseurStatus.FINALIZED);
        Date actionDate = new SimpleDateFormat("dd/MM/yyyy").parse("20/03/2019");
        classeur.setActions(Collections.singletonList(new Action(1, "agent-pes", actionDate, "signature", "RAS")));
        return classeur;
    }

    private JsonNode profileNode() throws IOException {
        String profile = "{ \"email\": \"agent@sictiam.fr\" }";
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(profile);
    }
 }

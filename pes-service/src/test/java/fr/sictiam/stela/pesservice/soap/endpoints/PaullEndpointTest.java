package fr.sictiam.stela.pesservice.soap.endpoints;

import fr.sictiam.stela.pesservice.model.PesAller;
import fr.sictiam.stela.pesservice.model.PesHistory;
import fr.sictiam.stela.pesservice.model.StatusType;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import static fr.sictiam.stela.pesservice.TestDataGenerator.*;
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

    private DateTimeFormatter dateFormatterWithSeconds = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private DateTimeFormatter dateFormatterWithMinutes = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
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

        LocalDateTime now = LocalDateTime.now();

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
                    "<ns2:dateDepot>" + dateFormatterWithMinutes.format(now) + "</ns2:dateDepot>" +
                    "<ns2:dateAR>" + dateFormatter.format(now) + "</ns2:dateAR>" +
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
                        "<ns2:dateAction>" + dateFormatterWithSeconds.format(now) + "</ns2:dateAction>" +
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
                .willReturn(pesAller(now));
        given(sesileService.getClasseur(any(), any()))
                .willReturn(Either.right(classeur(now)));
        given(externalRestService.getProfile(any()))
                .willReturn(profileNode());
        given(pesAllerService.getPesHistoryByTypes(any(), any()))
                .willReturn(Collections.singletonList(pesHistory(now)));

        mockClient.sendRequest(withPayload(new StringSource(detailsPesAllerRequest)))
                .andExpect(payload(new StringSource(detailsPesAllerResponse)));
    }

    private PesAller pesAller(LocalDateTime localDateTime) {
        PesAller pesAller = new PesAller(localDateTime, "objet", null, null, null,
                "profile-uuid", "comment", "PDF", "code", "postid",
                "budcode", "pes.xml", false, true, 1234, false);
        pesAller.setLastHistoryStatus(StatusType.ACK_RECEIVED);
        pesAller.setSesileClasseurId(4321);
        return pesAller;
    }

    public PesHistory pesHistory(LocalDateTime localDateTime) {
        return new PesHistory("pes-history-uuid", StatusType.ACK_RECEIVED, localDateTime);
    }
}

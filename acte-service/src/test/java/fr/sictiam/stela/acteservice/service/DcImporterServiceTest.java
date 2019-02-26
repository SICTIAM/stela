package fr.sictiam.stela.acteservice.service;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.model.LocalAuthority;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientResponseException;
import wiremock.com.google.common.net.HttpHeaders;
import wiremock.org.apache.http.entity.ContentType;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = DcImporterService.class,
        properties = {
                "application.datacore.dataOwnerSiren = 123456789",
                "application.datacore.dcImporterUrl = http://localhost:8089",
                "application.datacore.baseUri = https://data.ozwillo.com/dc/type",
                "application.url = https://stela3.sictiam.fr"})
@AutoConfigureWebClient(registerRestTemplate = true)
public class DcImporterServiceTest {

    @SpyBean
    private DcImporterService dcImporterService;

    @MockBean
    private ExternalRestService externalRestService;

    @MockBean
    private LocalAuthorityService localAuthorityService;

    @MockBean
    private ActeService acteService;

    @ClassRule
    static public WireMockRule wireMockRule = new WireMockRule(8089);

    @Test
    public void newResource() {
        stubFor(post(urlPathEqualTo("/dc/type/legal:type_0")).willReturn(created()));

        ResponseEntity responseEntity = dcImporterService.newResource("legal:type_0", "test_0", this.resourceProperties(), "access-token-test");

        WireMock.verify(postRequestedFor(urlPathEqualTo("/dc/type/legal:type_0"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer access-token-test"))
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(ContentType.APPLICATION_JSON.getMimeType()))
                .withHeader("X-Datacore-Project", equalTo("test_0"))
                .withRequestBody(equalToJson(this.resourceRequestBody())));

        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.CREATED);
    }

    @Test(expected = RestClientResponseException.class)
    public void newResourceThrowsException() {
        stubFor(post(urlPathEqualTo("/dc/type/legal:type_0")).willReturn(badRequest()));

        dcImporterService.newResource("legal:type_0", "test_0", this.resourceProperties(), "access-token-test");

        WireMock.verify(postRequestedFor(urlPathEqualTo("/dc/type/legal:type_0"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer access-token-test"))
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(ContentType.APPLICATION_JSON.getMimeType()))
                .withHeader("X-Datacore-Project", equalTo("test_0"))
                .withRequestBody(equalToJson(this.resourceRequestBody())));
    }

    @Test
    public void getAccessTokenForDataOwner() {
        LocalAuthority localAuthority = new LocalAuthority();
        ReflectionTestUtils.setField(localAuthority, "uuid", "uuid-local-authority-one-test");

        given(localAuthorityService.getBySiren("123456789")).willReturn(Optional.of(localAuthority));
        given(externalRestService.getAccessTokenFromKernel(any())).willReturn(Optional.of("access-token"));

        Optional<String> accessToken = dcImporterService.getAccessTokenForDataOwner();

        Assert.assertTrue(accessToken.isPresent());
    }

    @Test
    public void sendPublicActeToDcImporterSuccessful() {
        stubFor(post(urlPathEqualTo("/dc/type/legal:deliberation_0")).willReturn(ok()));
        LocalAuthority localAuthority = new LocalAuthority();
        ReflectionTestUtils.setField(localAuthority, "uuid", "uuid-local-authority-one-test");
        localAuthority.setSiren("123456789");

        Acte acte = new Acte();
        ReflectionTestUtils.setField(acte, "uuid", "acte-one-uuid-test");
        acte.setObjet("Acte test one");
        acte.setCreation(LocalDateTime.of(2019, 2, 8, 13, 0, 0));
        acte.setCode("5-4-0-0-0");
        acte.setCodeLabel("Institutions et vie politique / Delegation de fonctions");
        acte.setPublic(true);
        acte.setLocalAuthority(localAuthority);

        given(externalRestService.getLocalAuthorityDcId(localAuthority))
                .willReturn(Optional.empty());

        given(localAuthorityService.getBySiren("123456789")).willReturn(Optional.of(localAuthority));

        given(externalRestService.getAccessTokenFromKernel(localAuthority)).willReturn(Optional.of("access-token-test"));

        given(dcImporterService.newResource("legal:deliberation_0", "legal_0", this.resourceProperties(), "access-token-test"))
                .willReturn(new ResponseEntity<>(HttpStatus.CREATED));

        dcImporterService.sendPublicActeToDcImporter(acte);

        WireMock.verify(postRequestedFor(urlPathEqualTo("/dc/type/legal:deliberation_0"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer access-token-test"))
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(ContentType.APPLICATION_JSON.getMimeType()))
                .withHeader("X-Datacore-Project", equalTo("legal_0"))
                .withRequestBody(equalToJson(this.resourceRequestBody(), true, false)));

        verify(dcImporterService, times(1)).newResource(any(), any(), any(), any());
    }

    @Test
    public void sendPublicActeToDcImporterWithOraganizationDcIdNotGenerated() {
        stubFor(post(urlPathEqualTo("/dc/type/legal:deliberation_0")).willReturn(ok()));
        LocalAuthority localAuthority = new LocalAuthority();
        ReflectionTestUtils.setField(localAuthority, "uuid", "uuid-local-authority-one-test");
        localAuthority.setSiren("162549845621");

        Acte acte = new Acte();
        ReflectionTestUtils.setField(acte, "uuid", "acte-two-uuid-test");
        acte.setObjet("Acte test two");
        acte.setCreation(LocalDateTime.of(2019, 3, 12, 13, 0, 0));
        acte.setCode("2-2-0-0-0");
        acte.setCodeLabel("Developpement et tests / test unit");
        acte.setPublic(true);
        acte.setLocalAuthority(localAuthority);

        given(externalRestService.getLocalAuthorityDcId(localAuthority))
                .willReturn(Optional.of("https://data.sictiam.fr/dc/type/orgfr:Organisation_0/FR/162549845621"));

        given(localAuthorityService.getBySiren("123456789"))
                .willReturn(Optional.of(localAuthority));

        given(externalRestService.getAccessTokenFromKernel(localAuthority))
                .willReturn(Optional.of("access-token-test"));

        doReturn(new ResponseEntity<>(HttpStatus.CREATED)).when(dcImporterService)
                .newResource("legal:deliberation_0", "legal_0", this.resourceProperties(), "access-token-test");

        dcImporterService.sendPublicActeToDcImporter(acte);

        WireMock.verify(postRequestedFor(urlPathEqualTo("/dc/type/legal:deliberation_0"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer access-token-test"))
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(ContentType.APPLICATION_JSON.getMimeType()))
                .withHeader("X-Datacore-Project", equalTo("legal_0"))
                .withRequestBody(equalToJson(this.resourceRequestBodyWithDcIdNotGenerated(), true, false)));

        verify(dcImporterService, times(1)).newResource(any(), any(), any(), any());
    }

    private String resourceRequestBody() {
        return "{\n" +
                "\"@id\": \"https://data.ozwillo.com/dc/type/legal:deliberation_0/FR/123456789/acte-one-uuid-test/2019-02-08/5-4-0-0-0\",\n" +
                "\"deliberation:collectivite\":\"https://data.ozwillo.com/dc/type/orgfr:Organisation_0/FR/123456789\",\n" +
                "\"deliberation:delib_id\":\"acte-one-uuid-test\",\n" +
                "\"deliberation:delib_date\":\"2019-02-08\",\n" +
                "\"deliberation:delib_matiere_code\":\"5.4\",\n" +
                "\"deliberation:delib_matiere_nom\":\"Institutions et vie politique / Delegation de fonctions\",\n" +
                "\"deliberation:delib_objet\":\"Acte test one\",\n" +
                "\"deliberation:delib_url\":\"https://stela3.sictiam.fr/api/acte/public/acte-one-uuid-test/file?disposition=attachment\"\n" +
                "}";
    }

    private String resourceRequestBodyWithDcIdNotGenerated() {
        return "{\n" +
                "\"@id\": \"https://data.ozwillo.com/dc/type/legal:deliberation_0/FR/162549845621/acte-two-uuid-test/2019-03-12/2-2-0-0-0\",\n" +
                "\"deliberation:collectivite\":\"https://data.sictiam.fr/dc/type/orgfr:Organisation_0/FR/162549845621\",\n" +
                "\"deliberation:delib_id\":\"acte-two-uuid-test\",\n" +
                "\"deliberation:delib_date\":\"2019-03-12\",\n" +
                "\"deliberation:delib_matiere_code\":\"2.2\",\n" +
                "\"deliberation:delib_matiere_nom\":\"Developpement et tests / test unit\",\n" +
                "\"deliberation:delib_objet\":\"Acte test two\",\n" +
                "\"deliberation:delib_url\":\"https://stela3.sictiam.fr/api/acte/public/acte-two-uuid-test/file?disposition=attachment\"\n" +
                "}";
    }

    private Map<String, Object> resourceProperties() {
        Map<String, Object> resource = new HashMap<>();

        resource.put("@id", "https://data.ozwillo.com/dc/type/legal:deliberation_0/FR/123456789/acte-one-uuid-test/2019-02-08/5-4-0-0-0");
        resource.put("deliberation:collectivite", "https://data.ozwillo.com/dc/type/orgfr:Organisation_0/FR/123456789");
        resource.put("deliberation:delib_id", "acte-one-uuid-test");
        resource.put("deliberation:delib_date", "2019-02-08");
        resource.put("deliberation:delib_matiere_code", "5.4");
        resource.put("deliberation:delib_matiere_nom", "Institutions et vie politique / Delegation de fonctions");
        resource.put("deliberation:delib_objet", "Acte test one");
        resource.put("deliberation:delib_url", "https://stela3.sictiam.fr/api/acte/public/acte-one-uuid-test/file?disposition=attachment");

        return resource;
    }
}
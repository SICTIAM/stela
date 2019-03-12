package fr.sictiam.stela.apigateway.service;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import fr.sictiam.stela.apigateway.util.DiscoveryUtils;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.matches;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = LocalesService.class)
@AutoConfigureWebClient(registerRestTemplate = true)
public class LocalesServiceTest {

    @SpyBean
    private LocalesService localesService;

    @MockBean
    private DiscoveryUtils discoveryUtils;

    @MockBean
    private ModulesService modulesService;

    @ClassRule
    static public WireMockRule wireMockRule = new WireMockRule(8089);

    @Test
    public void getApiGatewayJsonTranslation() {
        String jsonTranslation = localesService.getJsonTranslation("api-gateway", "fr", "api-gateway");

        verifyZeroInteractions(discoveryUtils);
        verifyZeroInteractions(modulesService);
        assertThat(jsonTranslation, notNullValue());
    }

    @Test
    public void getExternalModuleJsonTranslation() {
        stubFor(get(urlPathMatching("/api/pes/locales/fr/pes.json"))
                .willReturn(ok("{ \"field\": \"value\" }")
                        .withHeader("Content-Type", "application/json")));

        given(modulesService.moduleHaveInstance(matches("pes"))).willReturn(true);

        given(discoveryUtils.getServiceUrlByName(matches("pes"))).willReturn("http://localhost:8089");

        String jsonTranslation = localesService.getJsonTranslation("pes", "fr", "pes");

        assertEquals(jsonTranslation, "{ \"field\": \"value\" }");
    }
}
package fr.sictiam.stela.apigateway.service;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import fr.sictiam.stela.apigateway.util.DiscoveryUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = RightsService.class)
@AutoConfigureWebClient(registerRestTemplate = true)
public class RightsServiceTest {

    @SpyBean
    private RightsService rightsService;

    @MockBean
    private ModulesService modulesService;

    @MockBean
    private DiscoveryUtils discoveryUtils;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);

    @Before
    public void setUp() {
        stubFor(get(urlPathMatching("/api/acte/rights"))
                .willReturn(ok("[\"ACTE_DISPLAY\", \"ACTE_DEPOSIT\" ]")
                        .withHeader("Content-Type", "application/json")));

        stubFor(get(urlPathMatching("/api/pes/rights"))
                .willReturn(ok("[\"PES_DISPLAY\"]")
                        .withHeader("Content-Type", "application/json")));

        stubFor(get(urlPathMatching("/api/convocation/rights"))
                .willReturn(ok("[\"CONVOCATION_ADMIN\", \"CONVOCATION_DISPLAY\", \"CONVOCATION_DEPOSIT\" ]")
                        .withHeader("Content-Type", "application/json")));


        given(modulesService.getModules()).willReturn(Arrays.asList("convocation", "pes", "acte"));

        given(discoveryUtils.getServiceUrlByName(matches("(acte|pes|convocation)"))).willReturn("http://localhost:8089");
    }

    @Test
    public void getRights() {
        given(modulesService.moduleHaveInstance(matches("(acte|pes|convocation)"))).willReturn(true);

        List<String> rights = rightsService.getRights();

        WireMock.verify(3, getRequestedFor(urlPathMatching("/api/(acte|pes|convocation)/rights")));

        Assert.assertEquals(6, rights.size());
    }

    @Test
    public void getRightsWithoutPESModuleInstance() {
        given(modulesService.moduleHaveInstance(matches("(acte|convocation)"))).willReturn(true);
        given(modulesService.moduleHaveInstance(matches("pes"))).willReturn(false);

        List<String> rights = rightsService.getRights();

        WireMock.verify(2, getRequestedFor(urlPathMatching("/api/(acte|convocation)/rights")));

        Assert.assertEquals(5, rights.size());
    }
}
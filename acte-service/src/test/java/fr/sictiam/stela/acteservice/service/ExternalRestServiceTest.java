package fr.sictiam.stela.acteservice.service;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import fr.sictiam.stela.acteservice.model.LocalAuthority;
import fr.sictiam.stela.acteservice.service.util.DiscoveryUtils;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = ExternalRestService.class,
        properties = {
                "kernel.tokenEndpoint = http://localhost:8089/a/token",
                "kernel.refreshToken = test-refresh-token"})
@AutoConfigureWebClient(registerRestTemplate = true)
public class ExternalRestServiceTest {

    @SpyBean
    private ExternalRestService externalRestService;

    @MockBean
    private DiscoveryUtils discoveryUtils;

    @MockBean
    private RestTemplate restTemplate;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);

    @Before
    public void setUp() {
        given(discoveryUtils.adminServiceUrl()).willReturn("http://localhost:8089");
    }

    @Test
    public void getAccessTokenFromKernel() {
        stubFor(get(urlPathEqualTo("/api/admin/local-authority/uuid-local-authority-one-test/accessToken"))
                .willReturn(ok("access-token")));

        LocalAuthority localAuthority = new LocalAuthority();
        ReflectionTestUtils.setField(localAuthority, "uuid", "uuid-local-authority-one-test");

        Optional<String> accessToken = externalRestService.getAccessTokenFromKernel(localAuthority);

        WireMock.verify(getRequestedFor(urlPathEqualTo("/api/admin/local-authority/uuid-local-authority-one-test/accessToken")));
        Assert.assertTrue(accessToken.isPresent());
    }

    @Test
    public void getAccessTokenFromKernelLocalAuthorityNotFound() {
        stubFor(get(urlPathEqualTo("/api/admin/local-authority/uuid-local-authority-two-test/accessToken"))
                .willReturn(notFound()));

        LocalAuthority localAuthority = new LocalAuthority();
        ReflectionTestUtils.setField(localAuthority, "uuid", "uuid-local-authority-two-test");

        Optional<String> accessToken = externalRestService.getAccessTokenFromKernel(localAuthority);

        WireMock.verify(getRequestedFor(urlPathEqualTo("/api/admin/local-authority/uuid-local-authority-two-test/accessToken")));
        Assert.assertFalse(accessToken.isPresent());
    }

    @Test
    public void getAccessTokenFromKernelErrorFromKernel() {
        stubFor(get(urlPathEqualTo("/api/admin/local-authority/uuid-local-authority-two-test/accessToken"))
                .willReturn(badRequest()));

        LocalAuthority localAuthority = new LocalAuthority();
        ReflectionTestUtils.setField(localAuthority, "uuid", "uuid-local-authority-two-test");

        Optional<String> accessToken = externalRestService.getAccessTokenFromKernel(localAuthority);

        WireMock.verify(getRequestedFor(urlPathEqualTo("/api/admin/local-authority/uuid-local-authority-two-test/accessToken")));
        Assert.assertFalse(accessToken.isPresent());
    }

    @Test
    public void getDcIdFromKernel() {
        stubFor(get(urlPathEqualTo("/api/admin/local-authority/uuid-local-authority-one-test/dcId"))
                .willReturn(ok("access-token")));

        LocalAuthority localAuthority = new LocalAuthority();
        ReflectionTestUtils.setField(localAuthority, "uuid", "uuid-local-authority-one-test");

        Optional<String> dcId = externalRestService.getLocalAuthorityDcId(localAuthority);

        WireMock.verify(getRequestedFor(urlPathEqualTo("/api/admin/local-authority/uuid-local-authority-one-test/dcId")));
        Assert.assertTrue(dcId.isPresent());
    }

    @Test
    public void getDcIdFromKernelLocalAuthorityNotFound() {
        stubFor(get(urlPathEqualTo("/api/admin/local-authority/uuid-local-authority-two-test/dcId"))
                .willReturn(notFound()));

        LocalAuthority localAuthority = new LocalAuthority();
        ReflectionTestUtils.setField(localAuthority, "uuid", "uuid-local-authority-two-test");

        Optional<String> dcId = externalRestService.getLocalAuthorityDcId(localAuthority);

        WireMock.verify(getRequestedFor(urlPathEqualTo("/api/admin/local-authority/uuid-local-authority-two-test/dcId")));
        Assert.assertFalse(dcId.isPresent());
    }

    @Test
    public void getDcIdFromKernelErrorFromKernel() {
        stubFor(get(urlPathEqualTo("/api/admin/local-authority/uuid-local-authority-two-test/dcId"))
                .willReturn(badRequest()));

        LocalAuthority localAuthority = new LocalAuthority();
        ReflectionTestUtils.setField(localAuthority, "uuid", "uuid-local-authority-two-test");

        Optional<String> dcId = externalRestService.getLocalAuthorityDcId(localAuthority);

        WireMock.verify(getRequestedFor(urlPathEqualTo("/api/admin/local-authority/uuid-local-authority-two-test/dcId")));
        Assert.assertFalse(dcId.isPresent());
    }
}
package fr.sictiam.stela.admin.service;

import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import fr.sictiam.stela.admin.dao.CertificateRepository;
import fr.sictiam.stela.admin.dao.LocalAuthorityRepository;
import fr.sictiam.stela.admin.dao.ProfileRepository;
import fr.sictiam.stela.admin.model.LocalAuthority;
import fr.sictiam.stela.admin.model.OzwilloInstanceInfo;
import fr.sictiam.stela.admin.model.TokenResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.persistence.EntityManagerFactory;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = LocalAuthorityService.class,
        properties = {
            "kernel.auth.token_endpoint = http://localhost:8089/a/token",
            "kernel.refreshToken = test-refresh-token"})
@AutoConfigureWebClient(registerRestTemplate = true)
public class LocalAuthorityServiceTest {

    @SpyBean
    private LocalAuthorityService localAuthorityService;

    @MockBean
    private LocalAuthorityRepository localAuthorityRepository;

    @MockBean
    private CertificateRepository certificateRepository;

    @MockBean
    private ProfileRepository profileRepository;

    @MockBean
    private EntityManagerFactory entityManagerFactory;

    @MockBean
    private AmqpTemplate amqpTemplate;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);

    @Before
    public void createWiremockStub() {
        stubFor(post(urlPathEqualTo("/a/token"))
                .willReturn(
                        okJson("{ \"access_token\": \"test-access-token\", \"token_type\": \"Bearer\", \"id_token\": \"test-id-token\" }")));
    }

    @Test
    public void getSyncAccessTokenFromKernel() {
        OzwilloInstanceInfo ozwilloInstanceInfo = new OzwilloInstanceInfo();
        ReflectionTestUtils.setField(ozwilloInstanceInfo, "clientId", "client-id-one");
        ReflectionTestUtils.setField(ozwilloInstanceInfo, "clientSecret", "client-secret-one");

        LocalAuthority localAuthority = new LocalAuthority("test-local-authority-uuid");
        localAuthority.setOzwilloInstanceInfo(ozwilloInstanceInfo);

        given(localAuthorityRepository.findBySiren("123456789")).willReturn(Optional.of(localAuthority));

        TokenResponse token = new TokenResponse();
        token.setAccessToken("access-token-test");
        token.setRefreshToken("refrech-token-test");

        Optional<TokenResponse> tokenResponseOptional =
                localAuthorityService.getAccessTokenFromKernel("123456789");

        TokenResponse tokenResponse = tokenResponseOptional.get();

        WireMock.verify(postRequestedFor(urlPathEqualTo("/a/token"))
                .withBasicAuth(new BasicCredentials("client-id-one", "client-secret-one"))
                .withRequestBody(equalTo("grant_type=refresh_token&refresh_token=test-refresh-token")));

        Assert.assertEquals(tokenResponse.getAccessToken(), "test-access-token");
        Assert.assertEquals(tokenResponse.getTokenType(), "Bearer");
        Assert.assertEquals(tokenResponse.getIdToken(), "test-id-token");
    }
}
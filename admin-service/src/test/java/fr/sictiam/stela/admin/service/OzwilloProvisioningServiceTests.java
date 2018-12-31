package fr.sictiam.stela.admin.service;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import fr.sictiam.stela.admin.model.LocalAuthority;
import fr.sictiam.stela.admin.model.OzwilloInstanceInfo;
import fr.sictiam.stela.admin.model.ProvisioningRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.FileCopyUtils;

import javax.persistence.EntityExistsException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = OzwilloProvisioningService.class,
    properties = { "ozwillo.service.localId = back-office",
            "application.url = http://localhost",
            "application.portalUrl = http://services.sictiam.fr"})
@AutoConfigureWebClient(registerRestTemplate = true)
public class OzwilloProvisioningServiceTests {

    @SpyBean
    private OzwilloProvisioningService provisioningService;

    @MockBean
    private LocalAuthorityService localAuthorityService;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);

    @Before
    public void createWiremockStub() {
        stubFor(post(urlPathEqualTo("/registration"))
                .willReturn(okJson("{ \"back-office\": \"567890\" }")));
    }

    @Test
    public void testProvisioningWithUnknownLocalAuthority() {

        doNothing().when(provisioningService).notifyRegistrationToKernel(any(), any());

        given(localAuthorityService.findBySiren("250601879"))
                .willReturn(Optional.empty());
        given(localAuthorityService.createOrUpdate(any()))
                .willReturn(any());

        ProvisioningRequest provisioningRequest = provisioningRequest();
        provisioningService.createNewInstance(provisioningRequest);

        verify(localAuthorityService).findBySiren("250601879");
        verify(localAuthorityService).createOrUpdate(argThat(localAuthority ->
                localAuthority.getSlugName().equals("valbonne-sur-mer")
                        && localAuthority.getName().equals("Valbonne sur mer")
                        && localAuthority.getSiren().equals("250601879")
                        && localAuthority.getOzwilloInstanceInfo().getClientId().equals("client-id")));
    }

    @Test(expected = EntityExistsException.class)
    public void testProvisioningWithExistingLocalAuthority() {

        given(localAuthorityService.findBySiren("250601879"))
                .willThrow(EntityExistsException.class);

        ProvisioningRequest provisioningRequest = provisioningRequest();
        provisioningService.createNewInstance(provisioningRequest);

        verify(localAuthorityService).findBySiren("250601879");
    }

    @Test
    public void testProvisioningAcknowledgement() throws IOException {

        LocalAuthority expectedLocalAuthority =
                new LocalAuthority("Valbonne sur mer", "250601879", "valbonne-sur-mer");
        expectedLocalAuthority.setOzwilloInstanceInfo(new OzwilloInstanceInfo());
        given(localAuthorityService.findByName("Valbonne sur mer"))
            .willReturn(Optional.of(expectedLocalAuthority));

        provisioningService.notifyRegistrationToKernel(provisioningRequest(), ozwilloInstanceInfo());

        verify(localAuthorityService).findByName("Valbonne sur mer");
        verify(localAuthorityService).modify(argThat(localAuthority ->
                localAuthority.getOzwilloInstanceInfo().getServiceId().equals("567890")
                        && localAuthority.getOzwilloInstanceInfo().isNotifiedToKernel()));

        File responseFile = (new ClassPathResource("/provisioning_response.json")).getFile();
        String provisioningResponse = FileCopyUtils.copyToString(new FileReader(responseFile));
        // I would prefer not having to ignore extra elements but it would required to mock static method
        // (ie RandonStringUtils.randomAlphanumeric(...) ... and that is not possible with Mockito
        // and so requires an extra lib and setup ...
        WireMock.verify(postRequestedFor(urlPathEqualTo("/registration"))
            .withRequestBody(equalToJson(provisioningResponse, false, true))
            .withHeader("Authorization", matching("Basic (.*)")));
    }

    private ProvisioningRequest provisioningRequest() {
        ProvisioningRequest.Organization organization =
                new ProvisioningRequest.Organization("12345", "Valbonne sur mer",
                "PUBLIC_BODY", "http://data.ozwillo.com/dc/type/orgfr:Organisation_0/FR/25060187900027");
        ProvisioningRequest.User user = new ProvisioningRequest.User("54321", "Jean Dupont");
        return new ProvisioningRequest("instance-id", "client-id", "client-secret", user, organization,
                "http://localhost:8089/registration", null);
    }

    private OzwilloInstanceInfo ozwilloInstanceInfo() {
        return new OzwilloInstanceInfo("12345", "instance-id", "client_id", "client-secret", null, null, null,
                "http://data.ozwillo.com/dc/type/orgfr:Organisation_0/FR/25060187900027");
    }
}

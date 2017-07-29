package fr.sictiam.stela.adminservice;

import fr.sictiam.stela.admin.AdminServiceApplication;
import fr.sictiam.stela.admin.model.LocalAuthority;
import fr.sictiam.stela.admin.model.ProvisioningRequest;
import fr.sictiam.stela.admin.service.LocalAuthorityService;
import fr.sictiam.stela.admin.service.OzwilloProvisioningService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AdminServiceApplication.class)
@WebAppConfiguration
public class OzwilloProvisioningServiceIntegrationTests extends BaseIntegrationTest {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OzwilloProvisioningService provisioningService;

    @Autowired
    private LocalAuthorityService localAuthorityService;

    @Test
    public void testExistingLocalAuthority() {

        MockRestServiceServer mockServer =
                MockRestServiceServer.bindTo(restTemplate).build();
        mockServer.expect(requestTo("http://localhost/registration"))
                .andRespond(withSuccess("{ \"back-office\": \"567890\" } ", MediaType.APPLICATION_JSON_UTF8));

        ProvisioningRequest provisioningRequest = provisioningRequest();
        provisioningService.createNewInstance(provisioningRequest);

        mockServer.verify();

        Optional<LocalAuthority> optLocalAuthority = localAuthorityService.findByName("Valbonne");
        Assert.assertTrue(optLocalAuthority.isPresent());
        LocalAuthority localAuthority = optLocalAuthority.get();
        Assert.assertEquals("25060187900027", localAuthority.getSiren());
        Assert.assertEquals("http://data.ozwillo.com/dc/type/orgfr:Organisation_0/FR/25060187900027", localAuthority.getOzwilloInstanceInfo().getDcId());
        Assert.assertEquals("54321", localAuthority.getOzwilloInstanceInfo().getCreatorId());
        Assert.assertTrue(localAuthority.getOzwilloInstanceInfo().isNotifiedToKernel());
        Assert.assertEquals("567890", localAuthority.getOzwilloInstanceInfo().getServiceId());

        Assert.assertTrue(localAuthorityService.findBySiren("25060187900027").isPresent());
    }

    private ProvisioningRequest provisioningRequest() {
        ProvisioningRequest.Organization organization =
                new ProvisioningRequest.Organization("12345", "Valbonne", "PUBLIC_BODY",
                        "http://data.ozwillo.com/dc/type/orgfr:Organisation_0/FR/25060187900027");
        ProvisioningRequest.User user = new ProvisioningRequest.User("54321", "Jean Dupont");
        return new ProvisioningRequest("instance-id", "client-id", "client-secret",
                user, organization, "http://localhost/registration", null);
    }
}

package fr.sictiam.stela.apigateway.service;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import fr.sictiam.stela.apigateway.util.DiscoveryUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = ModulesService.class)
@AutoConfigureWebClient(registerRestTemplate = true)
public class ModulesServiceTest {

    @Autowired
    private ModulesService modulesService;

    @MockBean
    private EurekaClient discoveryClient;

    @MockBean
    private DiscoveryUtils discoveryUtils;

    @Mock
    private InstanceInfo instanceInfo;

    @Mock
    private Application application;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);

    @Test
    public void getModules() {
        given(discoveryUtils.adminServiceUrl()).willReturn("http://localhost:8089");
        stubFor(get(urlPathMatching("/api/admin/modules"))
                .willReturn(ok("[ \"PES\", \"CONVOC\", \"ACTES\" ]")
                        .withHeader("Content-Type", "application/json; charset=UTF-8")));

        List<String> modules = modulesService.getModules();

        Assert.assertEquals(3, modules.size());
    }

    @Test
    public void moduleHaveInstance() {
        ReflectionTestUtils.setField(this.instanceInfo, "appName", "pes-service");

        List<InstanceInfo> instances = new ArrayList<>();
        instances.add(this.instanceInfo);

        given(discoveryClient.getApplication(matches("PES-SERVICE"))).willReturn(application);
        given(application.getInstances()).willReturn(instances);
        boolean moduleHaveInstance = modulesService.moduleHaveInstance("pes");

        Assert.assertTrue(moduleHaveInstance);
    }

    @Test
    public void moduleNotHaveInstance() {
        List<InstanceInfo> instances = new ArrayList<>();

        given(discoveryClient.getApplication(matches("PES-SERVICE"))).willReturn(application);
        given(application.getInstances()).willReturn(instances);
        boolean moduleHaveInstance = modulesService.moduleHaveInstance("pes");

        Assert.assertFalse(moduleHaveInstance);
    }
}
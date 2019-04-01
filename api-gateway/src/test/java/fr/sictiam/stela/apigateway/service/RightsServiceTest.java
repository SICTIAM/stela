package fr.sictiam.stela.apigateway.service;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import fr.sictiam.stela.apigateway.util.DiscoveryUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

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

    @MockBean
    private EurekaClient discoveryClient;

    @Mock
    private InstanceInfo instanceInfo;

    @Mock
    private InstanceInfo instanceInfo1;

    @Mock
    private Application application;

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


        given(discoveryUtils.getServiceUrlByName(matches("(acte|pes|convocation)"))).willReturn("http://localhost:8089");
        given(modulesService.extractServiceName("acte-service")).willReturn("acte");
        given(modulesService.extractServiceName("pes-service")).willReturn("pes");
        given(modulesService.extractServiceName("convocation-service")).willReturn("convocation");
    }

    @Test
    public void getRights() {
        InstanceInfo instanceInfoActe = this.instanceInfo("acte", "acte-service", "BUSINESS");
        InstanceInfo instanceInfoPes = this.instanceInfo("pes", "pes-service", "BUSINESS");
        InstanceInfo instanceInfoConvocation = this.instanceInfo("convocation", "convocation-service", "BUSINESS");

        Application applicationActe = this.application(instanceInfoActe, "acte-service");
        Application applicationPes = this.application(instanceInfoPes, "pes-service");
        Application applicationConvocation = this.application(instanceInfoConvocation, "convocation-service");


        List<Application> applications = new ArrayList<>();

        applications.add(applicationActe);
        applications.add(applicationPes);
        applications.add(applicationConvocation);

        given(modulesService.activeBusinessApplications()).willReturn(applications);

        List<String> rights = rightsService.getRights();

        Assert.assertEquals(6, rights.size());
    }

    @Test
    public void getRightsWithoutPESModuleInstance() {
        InstanceInfo instanceInfoActe = this.instanceInfo("acte", "acte-service", "BUSINESS");
        InstanceInfo instanceInfoConvocation = this.instanceInfo("convocation", "convocation-service", "BUSINESS");

        Application applicationActe = this.application(instanceInfoActe, "acte-service");
        Application applicationConvocation = this.application(instanceInfoConvocation, "convocation-service");


        List<Application> applications = new ArrayList<>();

        applications.add(applicationActe);
        applications.add(applicationConvocation);

        given(modulesService.activeBusinessApplications()).willReturn(applications);

        List<String> rights = rightsService.getRights();

        WireMock.verify(2, getRequestedFor(urlPathMatching("/api/(acte|convocation)/rights")));

        Assert.assertEquals(5, rights.size());
    }

    private InstanceInfo instanceInfo(String name, String serviceName, String appGroupName) {
        InstanceInfo instanceInfo = mock(InstanceInfo.class);
        Map<String, String> metadata = new ConcurrentHashMap<>();
        metadata.put("name", name);
        ReflectionTestUtils.setField(instanceInfo, "instanceId", serviceName);
        ReflectionTestUtils.setField(instanceInfo, "appName", serviceName);
        ReflectionTestUtils.setField(instanceInfo, "appGroupName", appGroupName);
        ReflectionTestUtils.setField(instanceInfo, "homePageUrl", "http://localhost:8089");
        ReflectionTestUtils.setField(instanceInfo, "metadata", metadata);

        return instanceInfo;
    }

    private Application application(InstanceInfo instanceInfo, String name) {
        Application application = new Application();

        List<InstanceInfo> instanceInfosList = new ArrayList<>();
        instanceInfosList.add(instanceInfo);

        AtomicReference<List<InstanceInfo>> shuffledInstances = new AtomicReference<>();
        shuffledInstances.set(instanceInfosList);

        Map<String, InstanceInfo> instancesMap = new ConcurrentHashMap<>();
        instancesMap.put(name, instanceInfo);

        ReflectionTestUtils.setField(application, "name", name);
        ReflectionTestUtils.setField(application, "shuffledInstances", shuffledInstances);
        ReflectionTestUtils.setField(application, "instancesMap", instancesMap);

        return application;
    }
}
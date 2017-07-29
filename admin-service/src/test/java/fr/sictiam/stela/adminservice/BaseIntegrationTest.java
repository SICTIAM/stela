package fr.sictiam.stela.adminservice;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.configuration.ProjectName;
import com.palantir.docker.compose.connection.DockerPort;
import com.palantir.docker.compose.connection.waiting.HealthCheck;
import com.palantir.docker.compose.connection.waiting.SuccessOrFailure;
import fr.sictiam.stela.admin.AdminServiceApplication;
import fr.sictiam.stela.admin.controller.LocalAuthorityController;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@Category(IntegrationTest.class)
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AdminServiceApplication.class)
@WebAppConfiguration
public class BaseIntegrationTest {

    @Autowired
    private LocalAuthorityController localAuthorityController;

    private static HealthCheck<DockerPort> toBeOpen() {
        return port -> SuccessOrFailure.fromBoolean(port.isListeningNow(), "" + port + "was not listening");
    }

    @ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder()
            .file("src/test/resources/docker-compose-test.yml")
            .projectName(ProjectName.random())
            .waitingForHostNetworkedPort(5432, toBeOpen())
            .saveLogsTo("build/dockerLogs/ozwilloProvisioningIntegrationTest")
            .build();

    @Test
    public void contexLoads() throws Exception {
        assertThat(localAuthorityController).isNotNull();
    }
}

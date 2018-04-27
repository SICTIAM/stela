package fr.sictiam.stela.pesservice;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.configuration.ProjectName;
import com.palantir.docker.compose.connection.DockerPort;
import com.palantir.docker.compose.connection.waiting.HealthCheck;
import com.palantir.docker.compose.connection.waiting.SuccessOrFailure;
import org.junit.ClassRule;
import org.junit.experimental.categories.Category;

@Category(IntegrationTests.class)
public abstract class BaseIntegrationTests {

    private static HealthCheck<DockerPort> toBeOpen() {
        return port -> SuccessOrFailure.fromBoolean(port.isListeningNow(), "" + port + "was not listening");
    }

    @ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder()
            .file("src/test/resources/docker-compose-test.yml").projectName(ProjectName.random())
            .waitingForHostNetworkedPort(5432, toBeOpen()).saveLogsTo("build/dockerLogs/integrationTests").build();
}

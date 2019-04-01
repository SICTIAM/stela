package fr.sictiam.stela.apigateway.service;

import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ModulesService {

    private final EurekaClient discoveryClient;

    public ModulesService(EurekaClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    public boolean moduleHaveInstance(String moduleName) {
        Application application = discoveryClient.getApplication(moduleName.concat("-service").toUpperCase());
        return application != null && !application.getInstances().isEmpty();
    }

    public List<Application> activeBusinessApplications() {
        return discoveryClient.getApplications().getRegisteredApplications().stream()
                .filter(Objects::nonNull)
                .filter(application -> !application.getInstances().isEmpty())
                .filter(application ->
                        application.getInstances().stream()
                                .anyMatch(instance ->
                                        instance.getAppGroupName() != null && instance.getAppGroupName().equals("BUSINESS")))
                .collect(Collectors.toList());
    }

    public String extractServiceName(String serviceName) {
        return serviceName.split("-")[0];
    }
}

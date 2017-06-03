package fr.sictiam.stela.apigateway.controller;

import com.netflix.hystrix.contrib.javanica.annotation.DefaultProperties;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import fr.sictiam.stela.apigateway.client.PesClient;
import fr.sictiam.stela.apigateway.dto.PesDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/pes")
// not the optimal setup but it seems sharing Spring security context in OAuth2 config is not quite fully functional
@DefaultProperties(commandProperties = {
    @HystrixProperty(name="execution.isolation.strategy", value="SEMAPHORE")
})
public class PesGatewayController {

    private final PesClient pesClient;

    public PesGatewayController(PesClient pesClient) {
        this.pesClient = pesClient;
    }

    @GetMapping("")
    @HystrixCommand(fallbackMethod = "getAllFallback")
    public List<PesDto> getAll() {
        return pesClient.getAll();
    }

    public List<PesDto> getAllFallback() { return Collections.emptyList(); }
}

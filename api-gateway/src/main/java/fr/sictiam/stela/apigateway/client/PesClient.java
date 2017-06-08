package fr.sictiam.stela.apigateway.client;

import fr.sictiam.stela.apigateway.config.FeignConfig;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@FeignClient(value = "pes-service", configuration = FeignConfig.class)
public interface PesClient {

    @RequestMapping(value = "/api/pes", method = RequestMethod.GET)
    List<Object> getAll();
}

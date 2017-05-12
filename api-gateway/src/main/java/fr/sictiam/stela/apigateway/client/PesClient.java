package fr.sictiam.stela.apigateway.client;

import fr.sictiam.stela.apigateway.dto.PesDto;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@FeignClient("pes-command")
public interface PesClient {

    @RequestMapping(value = "/pes-query", method = RequestMethod.GET)
    List<PesDto> getAll();
}

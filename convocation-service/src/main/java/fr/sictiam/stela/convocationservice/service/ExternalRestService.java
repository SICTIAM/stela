package fr.sictiam.stela.convocationservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sictiam.stela.convocationservice.service.util.DiscoveryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Optional;

@Service
public class ExternalRestService {

    @Autowired
    private DiscoveryUtils discoveryUtils;

    @Autowired
    private RestTemplate restTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalRestService.class);

    /*
    public JsonNode getProfile(String profileUuid) throws IOException {
        WebClient webClient = WebClient.create(discoveryUtils.adminServiceUrl());
        Mono<String> profile = webClient.get().uri("/api/admin/profile/{uuid}", profileUuid).retrieve()
                .onStatus(HttpStatus::is4xxClientError,
                        response -> Mono.error(new RuntimeException("Profile not Found")))
                .bodyToMono(String.class);

        Optional<String> opt = profile.blockOptional();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = objectMapper.readTree(opt.get());

        return node;
    }
 */
    public JsonNode getProfiles(String uuid) throws IOException {
        WebClient webClient = WebClient.create(discoveryUtils.adminServiceUrl());
        Mono<String> profiles = webClient.get().uri("/api/admin/local-authority/{uuid}/profiles", uuid).retrieve()
                .onStatus(HttpStatus::is4xxClientError,
                        response -> Mono.error(new RuntimeException("Profiles not Found")))
                .bodyToMono(String.class);

        Optional<String> opt = profiles.blockOptional();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = objectMapper.readTree(opt.get());

        return node;
    }


    public JsonNode getProfile(String uuid) {
        try {
            return restTemplate.getForObject(discoveryUtils.adminServiceUrl() + "/api/admin/profile/{uuid}",
                    JsonNode.class, uuid);
        } catch (RestClientResponseException e) {
            LOGGER.error("Failed to retrieve profile for {} : {} ({})", uuid, e.getMessage(),
                    e.getResponseBodyAsString());
            return null;
        }
    }

}

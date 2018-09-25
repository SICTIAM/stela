package fr.sictiam.stela.acteservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sictiam.stela.acteservice.model.ui.GenericAccount;
import fr.sictiam.stela.acteservice.service.util.DiscoveryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ExternalRestService {

    @Autowired
    DiscoveryUtils discoveryUtils;

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalRestService.class);

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

    public JsonNode getProfileForEmail(String siren, String email) throws IOException {
        WebClient webClient = WebClient.create(discoveryUtils.adminServiceUrl());
        Mono<String> profile = webClient
                .get().uri("/api/admin/profile/local-authority/{siren}/{email}", siren, email).retrieve()
                .onStatus(HttpStatus::is4xxClientError, response -> Mono.empty())
                .bodyToMono(String.class);

        Optional<String> opt = profile.blockOptional();
        if (!opt.isPresent()) return null;
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = objectMapper.readTree(opt.get());

        return node;
    }

    public JsonNode getGroupsForLocalAuthority(String uuid) throws IOException {
        WebClient webClient = WebClient.create(discoveryUtils.adminServiceUrl());
        Mono<String> profile = webClient
                .get().uri("/api/admin/local-authority/{uuid}/group/rights-on-module/{moduleName}",
                        uuid, "ACTES").retrieve()
                .onStatus(HttpStatus::is4xxClientError, response ->
                        Mono.error(new RuntimeException("LocalAuthority not found")))
                .bodyToMono(String.class);

        Optional<String> opt = profile.blockOptional();
        if (!opt.isPresent()) return null;
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = objectMapper.readTree(opt.get());

        return node;
    }

    public JsonNode getProfiles(String uuid) throws IOException {
        WebClient webClient = WebClient.create(discoveryUtils.adminServiceUrl());
        Mono<String> profiles = webClient.get().uri("/api/admin/profile/local-authority/{uuid}", uuid).retrieve()
                .onStatus(HttpStatus::is4xxClientError,
                        response -> Mono.error(new RuntimeException("Profiles not Found")))
                .bodyToMono(String.class);

        Optional<String> opt = profiles.blockOptional();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = objectMapper.readTree(opt.get());

        return node;
    }

    public GenericAccount getGenericAccount(String uuid) throws IOException {
        WebClient webClient = WebClient.create(discoveryUtils.adminServiceUrl());
        Mono<GenericAccount> genericAccount = webClient.get().uri("/api/admin/generic_account/{uuid}", uuid).retrieve()
                .onStatus(HttpStatus::is4xxClientError,
                        response -> Mono.error(new RuntimeException("generic_account_not_found")))
                .bodyToMono(GenericAccount.class);

        Optional<GenericAccount> opt = genericAccount.blockOptional();

        return opt.get();
    }

    public GenericAccount authWithCertificate(String serial, String vendor) throws IOException {

        Map<String, String> body = new HashMap<>();
        body.put("serial", serial);
        body.put("vendor", vendor);
        WebClient webClient = WebClient.create(discoveryUtils.adminServiceUrl());
        Mono<GenericAccount> genericAccount = webClient.post().uri("/api/admin/generic_account/authWithCertificate")
                .body(BodyInserters.fromObject(body)).retrieve()
                .onStatus(HttpStatus::is4xxClientError,
                        response -> Mono.error(new RuntimeException(
                                "No generic account for serial : " + serial + "and vendor : " + vendor)))
                .bodyToMono(GenericAccount.class);

        Optional<GenericAccount> opt = genericAccount.blockOptional();

        return opt.get();
    }

    public GenericAccount authWithEmailPassword(String email, String password) throws IOException {
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        WebClient webClient = WebClient.create(discoveryUtils.adminServiceUrl());
        Mono<GenericAccount> genericAccount = webClient.post().uri("/api/admin/generic_account/authWithEmailPassword")
                .body(BodyInserters.fromObject(body)).retrieve()
                .onStatus(HttpStatus::is4xxClientError,
                        response -> Mono.error(new RuntimeException("Account or password invalid")))
                .bodyToMono(GenericAccount.class);

        Optional<GenericAccount> opt = genericAccount.blockOptional();

        return opt.get();
    }

    public JsonNode getPaullConnection(String sessionID) throws IOException {
        WebClient webClient = WebClient.create(discoveryUtils.adminServiceUrl());
        Mono<String> profiles = webClient.get().uri("/api/admin/generic_account/session/{sessionID}", sessionID)
                .retrieve().onStatus(HttpStatus::is4xxClientError,
                        response -> Mono.error(new RuntimeException("Session not Found")))
                .bodyToMono(String.class);

        Optional<String> opt = profiles.blockOptional();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = objectMapper.readTree(opt.get());

        return node;
    }
}

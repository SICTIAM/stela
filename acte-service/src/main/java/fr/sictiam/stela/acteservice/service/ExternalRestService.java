package fr.sictiam.stela.acteservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sictiam.stela.acteservice.model.LocalAuthority;
import fr.sictiam.stela.acteservice.model.Right;
import fr.sictiam.stela.acteservice.model.ui.GenericAccount;
import fr.sictiam.stela.acteservice.model.util.Certificate;
import fr.sictiam.stela.acteservice.service.util.DiscoveryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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
                .get().uri("/api/admin/local-authority/{uuid}/{moduleName}/group",
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

    public LocalAuthority getLocalAuthorityByCertificate(Certificate certificate) {

        LocalAuthority localAuthority = null;
        try {
            RestTemplate restTemplate = new RestTemplate();
            localAuthority = restTemplate.getForObject(
                    discoveryUtils.adminServiceUrl() + "/api/admin/local-authority/certificate/{serial}/{issuer}",
                    LocalAuthority.class, certificate.getSerial(), certificate.getIssuer());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND)
                LOGGER.warn("No local authority for this certificate {}|{}", certificate.getSerial(), certificate.getIssuer());
            else
                LOGGER.error("Failed to find local authority from a certificate : {}", e.getMessage());
        } catch (RestClientException e) {
            LOGGER.error("Failed to find local authority from a certificate : {}", e.getMessage());
        }

        return localAuthority;
    }

    public String createGroup(LocalAuthority localAuthority, String name) {

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.postForObject(
                discoveryUtils.adminServiceUrl() + "/api/admin/local-authority/{localAuthorityUuid}/group/{name}",
                new HashSet<>(Arrays.stream(Right.values()).map(Right::toString).collect(Collectors.toSet())),
                String.class,
                localAuthority.getUuid(), name);
    }

    public Optional<String> getAccessTokenFromKernel(LocalAuthority localAuthority) {
        RestTemplate restTemplate = new RestTemplate();

        try {
            String accessToken = restTemplate.getForObject(
                    discoveryUtils.adminServiceUrl() + "/api/admin/local-authority/{localAuthoritySiren}/accessToken",
                    String.class,
                    localAuthority.getSiren());
            return Optional.ofNullable(accessToken);
        } catch (RestClientResponseException e){
            LOGGER.error(
                    "[getAccessTokenFromKernel] An error was occured when tried to get kernel access token for organization {} from {} : Status {} Body {} ",
                    discoveryUtils.adminServiceUrl(),
                    e.getRawStatusCode(),
                    e.getResponseBodyAsString());
            return Optional.empty();
        }
    }

    public Optional<String> getLocalAuthorityDcId(LocalAuthority localAuthority) {
        RestTemplate restTemplate = new RestTemplate();

        try {
            String dcId = restTemplate.getForObject(
                    discoveryUtils.adminServiceUrl() + "/api/admin/local-authority/{localAuthoritySiren}/dcId",
                    String.class,
                    localAuthority.getSiren());
            return Optional.ofNullable(dcId);
        } catch (RestClientResponseException e){
            LOGGER.error(
                    "[getLocalAuthorityDcId] An error was occured when tried to get DcId for organization {} from {} : Status {} Body {} ",
                    localAuthority.getSiren(),
                    discoveryUtils.adminServiceUrl(),
                    e.getRawStatusCode(),
                    e.getResponseBodyAsString());
            return Optional.empty();
        }
    }
}

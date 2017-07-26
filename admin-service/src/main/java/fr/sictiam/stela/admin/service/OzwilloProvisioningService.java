package fr.sictiam.stela.admin.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.sictiam.stela.admin.model.Agent;
import fr.sictiam.stela.admin.model.LocalAuthority;
import fr.sictiam.stela.admin.model.ProvisioningRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityExistsException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Collections;

@Service
public class OzwilloProvisioningService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OzwilloProvisioningService.class);

//    @Value("${kernel.auth.token_endpoint}")
//    private String tokenEndpoint;

    private final LocalAuthorityService localAuthorityService;
    private final AgentService agentService;

    public OzwilloProvisioningService(LocalAuthorityService localAuthorityService,
                                      AgentService agentService) {
        this.localAuthorityService = localAuthorityService;
        this.agentService = agentService;
    }

    public void createNewInstance(ProvisioningRequest provisioningRequest) {

        String dcId = provisioningRequest.getOrganization().getDcId();
        // SIRET is the last part of an organization URI
        String siret = dcId.substring(dcId.lastIndexOf('/') + 1);
        if (localAuthorityService.findBySiren(siret).isPresent())
            throw new EntityExistsException("There already exists a local authority with SIRET " + siret);
        LocalAuthority localAuthority = new LocalAuthority(provisioningRequest.getOrganization().getName(), siret);
        localAuthorityService.create(localAuthority);

        // TODO : store Ozwillo specific information, with not notified flag

        // TODO : would be better to have family and given names but it costs two requests ...
        //        (one to get the token and one to get the user infos)
        Agent agent = new Agent(provisioningRequest.getUser().getName(), "", provisioningRequest.getUser().getEmailAddress());
        agent.setSub(provisioningRequest.getUser().getId());
        agent.setAdmin(true);
        agentService.createIfNotExists(agent);

        // TODO : add the agent to the newly created local authority

        // TODO : call Ozwillo to instance_registration_uri and set notified flag if ok
    }

    // TODO : it will probably be not necessary, keeping it just in case
//    private TokenResponse getToken(ProvisioningRequest provisioningRequest) {
//
//        HttpHeaders httpHeaders = new HttpHeaders();
//        String clientInfo = provisioningRequest.getClientId() + ":" + provisioningRequest.getClientSecret();
//        try {
//            httpHeaders.add("Authorization", "Basic " + Base64.getEncoder().encodeToString(clientInfo.getBytes("UTF-8")));
//        } catch (UnsupportedEncodingException e) {
//            LOGGER.error("Non realistic encoding exception !");
//        }
//        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//
//        MultiValueMap<String, String> formParameters = new LinkedMultiValueMap<>();
//        formParameters.add("grant_type", provisioningRequest.getAuthorizationGrant().getGrantType());
//        formParameters.add("assertion", provisioningRequest.getAuthorizationGrant().getAssertion());
//        formParameters.add("scope", provisioningRequest.getAuthorizationGrant().getScope());
//        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formParameters, httpHeaders);
//
//        RestTemplate restTemplate = new RestTemplate();
//        restTemplate.setMessageConverters(Collections.singletonList(new FormHttpMessageConverter()));
//        // TODO : add handler for exceptions (401, ...)
//        ResponseEntity<TokenResponse> tokenResponse =
//                restTemplate.exchange(tokenEndpoint, HttpMethod.POST, entity, TokenResponse.class);
//
//        return tokenResponse.getBody();
//    }
//
//    private class TokenResponse {
//        @JsonProperty(value = "access_token")
//        private String accessToken;
//        @JsonProperty(value = "token_type")
//        private String tokenType;
//
//        public TokenResponse() {
//        }
//
//        public String getAccessToken() {
//            return accessToken;
//        }
//
//        public String getTokenType() {
//            return tokenType;
//        }
//    }
}

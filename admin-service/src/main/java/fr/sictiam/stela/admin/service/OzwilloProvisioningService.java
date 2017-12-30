package fr.sictiam.stela.admin.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.slugify.Slugify;
import fr.sictiam.stela.admin.model.LocalAuthority;
import fr.sictiam.stela.admin.model.OzwilloInstanceInfo;
import fr.sictiam.stela.admin.model.ProvisioningRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityExistsException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@EnableConfigurationProperties(OzwilloProvisioningService.OzwilloServiceProperties.class)
public class OzwilloProvisioningService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OzwilloProvisioningService.class);

    @ConfigurationProperties(prefix = "ozwillo.service")
    public static class OzwilloServiceProperties {
        private String localId;
        private String name;
        private String description;
        private String tosUri;
        private String policyUri;
        private String icon;
        private List<String> contacts;

        public void setLocalId(String localId) {
            this.localId = localId;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setTosUri(String tosUri) {
            this.tosUri = tosUri;
        }

        public void setPolicyUri(String policyUri) {
            this.policyUri = policyUri;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public void setContacts(List<String> contacts) {
            this.contacts = contacts;
        }
    }

    @Value("${application.url}")
    private String applicationUrl;

    @Value("${application.urlWithSlug}")
    private String applicationUrlWithSlug;

    private final LocalAuthorityService localAuthorityService;
    private final OzwilloServiceProperties ozwilloServiceProperties;
    private final RestTemplate restTemplate;

    @Autowired
    public OzwilloProvisioningService(LocalAuthorityService localAuthorityService,
                                      OzwilloServiceProperties ozwilloServiceProperties, RestTemplate restTemplate) {
        this.localAuthorityService = localAuthorityService;
        this.ozwilloServiceProperties = ozwilloServiceProperties;
        this.restTemplate = restTemplate;
    }

    public void createNewInstance(ProvisioningRequest provisioningRequest) {

        String dcId = provisioningRequest.getOrganization().getDcId();
        // SIRET is the last part of an organization URI
        String siret = dcId.substring(dcId.lastIndexOf('/') + 1);
        if (localAuthorityService.findBySiren(siret).isPresent())
            throw new EntityExistsException("There already exists a local authority with SIRET " + siret);
        String slugName = new Slugify().slugify(provisioningRequest.getOrganization().getName());
        LocalAuthority localAuthority = new LocalAuthority(provisioningRequest.getOrganization().getName(), siret, slugName);
        OzwilloInstanceInfo ozwilloInstanceInfo = new OzwilloInstanceInfo(provisioningRequest.getInstanceId(),
                provisioningRequest.getClientId(), provisioningRequest.getClientSecret(),
                provisioningRequest.getInstanceRegistrationUri(), provisioningRequest.getUser().getId(),
                provisioningRequest.getUser().getName(), provisioningRequest.getOrganization().getDcId());
        localAuthority.setOzwilloInstanceInfo(ozwilloInstanceInfo);
        localAuthorityService.createOrUpdate(localAuthority);

        CompletableFuture.runAsync(() -> notifyRegistrationToKernel(provisioningRequest, ozwilloInstanceInfo));
    }

    /**
     * Provider acknoledgement sent to Ozwillo's kernel.
     *
     * A sample response is like this :
     * {"instance_id":"bce53130-af7d-44a0-8a87-291a37f22e4c","destruction_uri":"https://sictiam.stela3-dev.sictiam.fr/api/admin/ozwillo/delete","destruction_secret":"secret","status_changed_uri":"https://sictiam.stela3-dev.sictiam.fr/api/admin/ozwillo/status","status_changed_secret":"secret","services":[{"local_id":"back-office","name":"STELA - SICTIAM","description":"Tiers de télétransmission","tos_uri":"https://stela.fr/tos","policy_uri":"https://stela.fr/policy","icon":"https://stela.fr/icon.png","contacts":["admin@stela.fr","demat@sictiam.fr"],"payment_option":"PAID","target_audience":"PUBLIC_BODY","visibility":"VISIBLE","access_control":"RESTRICTED","service_uri":"https://sictiam.stela3-dev.sictiam.fr/login","redirect_uris":["https://sictiam.stela3-dev.sictiam.fr/login"]}]}
     */
    private void notifyRegistrationToKernel(ProvisioningRequest provisioningRequest, OzwilloInstanceInfo ozwilloInstanceInfo) {
        HttpHeaders httpHeaders = new HttpHeaders();
        String clientInfo = provisioningRequest.getClientId() + ":" + provisioningRequest.getClientSecret();
        try {
            httpHeaders.add("Authorization", "Basic " + Base64.getEncoder().encodeToString(clientInfo.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Non realistic encoding exception !");
        }

        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);

        KernelInstanceRegistrationRequest kernelInstanceRegistrationRequest =
                new KernelInstanceRegistrationRequest(provisioningRequest, ozwilloInstanceInfo, ozwilloServiceProperties);
        LOGGER.debug("Generated kernel response {}", kernelInstanceRegistrationRequest);

        HttpEntity<KernelInstanceRegistrationRequest> request = new HttpEntity<>(kernelInstanceRegistrationRequest, httpHeaders);
        ResponseEntity<String> response =
                restTemplate.postForEntity(provisioningRequest.getInstanceRegistrationUri(), request, String.class);

        if (response.getStatusCode().is4xxClientError()) {
            LOGGER.error("Error while acknowledging instanciation response : {}", response.getBody());
        } else if (response.getStatusCode().is2xxSuccessful()) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                JsonNode node = mapper.readTree(response.getBody());
                String serviceId = node.get(ozwilloServiceProperties.localId).asText();
                LOGGER.debug("Got service id : {}", serviceId);
                LocalAuthority localAuthority =
                        localAuthorityService.findByName(provisioningRequest.getOrganization().getName()).get();
                localAuthority.getOzwilloInstanceInfo().setServiceId(serviceId);
                localAuthority.getOzwilloInstanceInfo().setNotifiedToKernel(true);
                localAuthorityService.modify(localAuthority);
            } catch (IOException e) {
                LOGGER.error("Error while reading kernel response", e);
            }
        } else {
            LOGGER.warn("Unknown status code returned from the kernel : {} ({})", response.getBody(), response.getStatusCodeValue());
        }
    }

    private class KernelInstanceRegistrationRequest {
        @JsonProperty("instance_id")
        private String instanceId;
        @JsonProperty("destruction_uri")
        private String destructionUri;
        @JsonProperty("destruction_secret")
        private String destructionSecret;
        @JsonProperty("status_changed_uri")
        private String statusChangedUri;
        @JsonProperty("status_changed_secret")
        private String statusChangedSecret;
        @JsonProperty("services")
        private List<Service> services;

        KernelInstanceRegistrationRequest(ProvisioningRequest provisioningRequest, OzwilloInstanceInfo ozwilloInstanceInfo,
                                          OzwilloServiceProperties ozwilloServiceProperties) {
            this.instanceId = ozwilloInstanceInfo.getInstanceId();
            this.destructionUri = applicationUrl + "/api/admin/ozwillo/delete";
            this.destructionSecret = ozwilloInstanceInfo.getDestructionSecret();
            this.statusChangedUri = applicationUrl + "/api/admin/ozwillo/status";
            this.statusChangedSecret = ozwilloInstanceInfo.getStatusChangedSecret();
            this.services = Collections.singletonList(new Service(ozwilloServiceProperties, provisioningRequest.getOrganization()));
        }

        @Override
        public String toString() {
            return "KernelInstanceRegistrationRequest{" +
                    "instanceId='" + instanceId + '\'' +
                    ", destructionUri='" + destructionUri + '\'' +
                    ", destructionSecret='" + destructionSecret + '\'' +
                    ", statusChangedUri='" + statusChangedUri + '\'' +
                    ", statusChangedSecret='" + statusChangedSecret + '\'' +
                    ", services=" + services +
                    '}';
        }

        private class Service {
            @JsonProperty("local_id")
            private String localId;
            @JsonProperty("name")
            private String name;
            @JsonProperty("description")
            private String description;
            @JsonProperty("tos_uri")
            private String tosUri;
            @JsonProperty("policy_uri")
            private String policyUri;
            @JsonProperty("icon")
            private String icon;
            @JsonProperty("contacts")
            private List<String> contacts;
            @JsonProperty("payment_option")
            private String paymentOption;
            @JsonProperty("target_audience")
            private List<String> targetAudience;
            @JsonProperty("visibility")
            private String visibility;
            @JsonProperty("access_control")
            private String accessControl;
            @JsonProperty("service_uri")
            private String serviceUri;
            @JsonProperty("redirect_uris")
            private List<String> redirectUris;

            Service(OzwilloServiceProperties ozwilloServiceProperties, ProvisioningRequest.Organization organization) {
                this.localId = ozwilloServiceProperties.localId;
                this.name = ozwilloServiceProperties.name + " - " + organization.getName();
                this.description = ozwilloServiceProperties.description;
                this.tosUri = ozwilloServiceProperties.tosUri;
                this.policyUri = ozwilloServiceProperties.policyUri;
                this.icon = ozwilloServiceProperties.icon;
                this.contacts = ozwilloServiceProperties.contacts;
                this.paymentOption = "PAID";
                this.targetAudience = Collections.singletonList("PUBLIC_BODIES");
                this.visibility = "VISIBLE";
                this.accessControl = "RESTRICTED";
                String applicationInstanceUrl = applicationUrlWithSlug.replace("%SLUG%", new Slugify().slugify(organization.getName()));
                this.serviceUri = applicationInstanceUrl + "/login";
                this.redirectUris = Collections.singletonList(applicationInstanceUrl + "/callback");
            }

            @Override
            public String toString() {
                return "Service{" +
                        "localId='" + localId + '\'' +
                        ", name='" + name + '\'' +
                        ", description='" + description + '\'' +
                        ", tosUri='" + tosUri + '\'' +
                        ", policyUri='" + policyUri + '\'' +
                        ", icon='" + icon + '\'' +
                        ", contacts=" + contacts +
                        ", paymentOption='" + paymentOption + '\'' +
                        ", targetAudience='" + targetAudience + '\'' +
                        ", visibility='" + visibility + '\'' +
                        ", accessControl='" + accessControl + '\'' +
                        ", serviceUri='" + serviceUri + '\'' +
                        ", redirectUris=" + redirectUris +
                        '}';
            }
        }
    }
}

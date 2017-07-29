package fr.sictiam.stela.admin.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sictiam.stela.admin.model.LocalAuthority;
import fr.sictiam.stela.admin.model.OzwilloInstanceInfo;
import fr.sictiam.stela.admin.model.ProvisioningRequest;
import org.apache.commons.lang3.StringUtils;
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

    @Value("${application.host.suffix}")
    private String hostSuffix;

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
        LocalAuthority localAuthority = new LocalAuthority(provisioningRequest.getOrganization().getName(), siret);
        OzwilloInstanceInfo ozwilloInstanceInfo = new OzwilloInstanceInfo(provisioningRequest.getInstanceId(),
                provisioningRequest.getClientId(), provisioningRequest.getClientSecret(),
                provisioningRequest.getInstanceRegistrationUri(), provisioningRequest.getUser().getId(),
                provisioningRequest.getUser().getName(), provisioningRequest.getOrganization().getId(),
                provisioningRequest.getOrganization().getDcId());
        localAuthority.setOzwilloInstanceInfo(ozwilloInstanceInfo);
        localAuthorityService.create(localAuthority);

        notifyRegistrationToKernel(provisioningRequest, ozwilloInstanceInfo);
    }

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
                restTemplate.exchange(provisioningRequest.getInstanceRegistrationUri(), HttpMethod.POST, request, String.class);

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
        private Service service;

        public KernelInstanceRegistrationRequest(ProvisioningRequest provisioningRequest, OzwilloInstanceInfo ozwilloInstanceInfo,
                                                 OzwilloServiceProperties ozwilloServiceProperties) {
            this.instanceId = ozwilloInstanceInfo.getInstanceId();
            this.destructionUri = getInstanceUri(provisioningRequest.getOrganization().getName(), "ozwillo/delete");
            this.destructionSecret = ozwilloInstanceInfo.getDestructionSecret();
            this.statusChangedUri = getInstanceUri(provisioningRequest.getOrganization().getName(), "ozwillo/status");
            this.statusChangedSecret = ozwilloInstanceInfo.getStatusChangedSecret();
            this.service = new Service(ozwilloServiceProperties, provisioningRequest.getOrganization());
        }

        @Override
        public String toString() {
            return "KernelInstanceRegistrationRequest{" +
                    "instanceId='" + instanceId + '\'' +
                    ", destructionUri='" + destructionUri + '\'' +
                    ", destructionSecret='" + destructionSecret + '\'' +
                    ", statusChangedUri='" + statusChangedUri + '\'' +
                    ", statusChangedSecret='" + statusChangedSecret + '\'' +
                    ", service=" + service +
                    '}';
        }

        private class Service {
            @JsonProperty("local_id")
            private String localId;
            private String name;
            private String description;
            @JsonProperty("tos_uri")
            private String tosUri;
            @JsonProperty("policy_uri")
            private String policyUri;
            private String icon;
            private List<String> contacts;
            @JsonProperty("payment_option")
            private String paymentOption;
            @JsonProperty("target_audience")
            private String targetAudience;
            private String visibility;
            @JsonProperty("access_control")
            private String accessControl;
            @JsonProperty("service_uri")
            private String serviceUri;
            @JsonProperty("redirect_uris")
            private List<String> redirectUris;

            public Service(OzwilloServiceProperties ozwilloServiceProperties, ProvisioningRequest.Organization organization) {
                this.localId = ozwilloServiceProperties.localId;
                this.name = ozwilloServiceProperties.name + " - " + organization.getName();
                this.description = ozwilloServiceProperties.description;
                this.tosUri = ozwilloServiceProperties.tosUri;
                this.policyUri = ozwilloServiceProperties.policyUri;
                this.icon = ozwilloServiceProperties.icon;
                this.contacts = ozwilloServiceProperties.contacts;
                this.paymentOption = "PAID";
                this.targetAudience = "PUBLIC_BODY";
                this.visibility = "VISIBLE";
                this.accessControl = "RESTRICTED";
                this.serviceUri = getInstanceUri(organization.getName(), "login");
                this.redirectUris = Collections.singletonList(getInstanceUri(organization.getName(), "login"));
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

    private String getInstanceUri(String organizationName, String uriPath) {
        String organizationNameForSubdomain = StringUtils.replacePattern(organizationName, "[^a-zA-Z]+", "").toLowerCase();
        return String.format("https://%s.%s/%s", organizationNameForSubdomain, hostSuffix, uriPath);
    }
}

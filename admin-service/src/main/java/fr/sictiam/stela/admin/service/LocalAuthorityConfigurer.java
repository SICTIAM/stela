package fr.sictiam.stela.admin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@EnableConfigurationProperties(LocalAuthorityConfigurer.OzwilloDefaultInstances.class)
@Profile({"dev", "dev-docker"})
public class LocalAuthorityConfigurer implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalAuthorityConfigurer.class);

    @ConfigurationProperties(prefix = "ozwillo")
    public static class OzwilloDefaultInstances {
        private Map<String, InstanceConfiguration> defaultInstances = new HashMap<>();

        public Map<String, InstanceConfiguration> getDefaultInstances() {
            return defaultInstances;
        }

        public static class InstanceConfiguration {
            private String client_id;
            private String client_secret;
            private String destruction_secret;
            private String status_changed_secret;

            public String getClient_id() {
                return client_id;
            }

            public void setClient_id(String client_id) {
                this.client_id = client_id;
            }

            public String getClient_secret() {
                return client_secret;
            }

            public void setClient_secret(String client_secret) {
                this.client_secret = client_secret;
            }

            public String getDestruction_secret() {
                return destruction_secret;
            }

            public void setDestruction_secret(String destruction_secret) {
                this.destruction_secret = destruction_secret;
            }

            public String getStatus_changed_secret() {
                return status_changed_secret;
            }

            public void setStatus_changed_secret(String status_changed_secret) {
                this.status_changed_secret = status_changed_secret;
            }
        }
    }

    private final OzwilloDefaultInstances ozwilloDefaultInstances;
    private final LocalAuthorityService localAuthorityService;

    @Autowired
    public LocalAuthorityConfigurer(OzwilloDefaultInstances ozwilloDefaultInstances,
                                    LocalAuthorityService localAuthorityService) {
        this.ozwilloDefaultInstances = ozwilloDefaultInstances;
        this.localAuthorityService = localAuthorityService;
    }

    @Override
    public void run(String... args) {
        ozwilloDefaultInstances.getDefaultInstances().forEach((key, value) ->
                localAuthorityService.getBySlugName(key).ifPresent(localAuthority -> {
                    if (localAuthority.getOzwilloInstanceInfo().getClientId().equals(value.client_id)) {
                        LOGGER.debug("Updating config for {}", key);
                        localAuthority.getOzwilloInstanceInfo().setClientSecret(value.client_secret);
                        localAuthority.getOzwilloInstanceInfo().setDestructionSecret(value.destruction_secret);
                        localAuthority.getOzwilloInstanceInfo().setStatusChangedSecret(value.destruction_secret);
                        localAuthorityService.modify(localAuthority);
                    } else {
                        LOGGER.info("Not updating {} since client id {} does not match with stored value {}",
                                key,
                                value.client_id,
                                localAuthority.getOzwilloInstanceInfo().getClientId());
                    }
                }));
    }
}

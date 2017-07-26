package fr.sictiam.stela.admin.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProvisioningRequest {

    @JsonProperty(value = "instance_id")
    private String instanceId;
    @JsonProperty(value = "client_id")
    private String clientId;
    @JsonProperty(value = "client_secret")
    private String clientSecret;
    @JsonProperty(value = "user")
    private User user;
    @JsonProperty(value = "organization")
    private Organization organization;
    @JsonProperty(value = "instance_registration_uri")
    private String instanceRegistrationUri;
    @JsonProperty(value = "authorization_grant")
    private AuthorizationGrant authorizationGrant;

    public ProvisioningRequest() {
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public User getUser() {
        return user;
    }

    public Organization getOrganization() {
        return organization;
    }

    public String getInstanceRegistrationUri() {
        return instanceRegistrationUri;
    }

    public AuthorizationGrant getAuthorizationGrant() {
        return authorizationGrant;
    }

    @Override
    public String toString() {
        return "ProvisioningRequest{" +
                "instanceId='" + instanceId + '\'' +
                ", clientId='" + clientId + '\'' +
                ", clientSecret='" + clientSecret + '\'' +
                ", user=" + user +
                ", organization=" + organization +
                ", instanceRegistrationUri='" + instanceRegistrationUri + '\'' +
                ", authorizationGrant=" + authorizationGrant +
                '}';
    }

    public class User {

        private String id;
        private String name;
        @JsonProperty(value = "email_address")
        private String emailAddress;

        public User() {
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getEmailAddress() {
            return emailAddress;
        }

        @Override
        public String toString() {
            return "User{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", emailAddress='" + emailAddress + '\'' +
                    '}';
        }
    }

    public class Organization {

        private String id;
        private String name;
        private String type;
        @JsonProperty(value = "dc_id")
        private String dcId;

        public Organization() {
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public String getDcId() {
            return dcId;
        }

        @Override
        public String toString() {
            return "Organization{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", dcId='" + dcId + '\'' +
                    '}';
        }
    }

    public class AuthorizationGrant {

        @JsonProperty(value = "grant_type")
        private String grantType;
        private String assertion;
        private String scope;

        public AuthorizationGrant() {
        }

        public String getGrantType() {
            return grantType;
        }

        public String getAssertion() {
            return assertion;
        }

        public String getScope() {
            return scope;
        }

        @Override
        public String toString() {
            return "AuthorizationGrant{" +
                    "grantType='" + grantType + '\'' +
                    ", assertion='" + assertion + '\'' +
                    ", scope='" + scope + '\'' +
                    '}';
        }
    }
}

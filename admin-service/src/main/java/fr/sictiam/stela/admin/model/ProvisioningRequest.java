package fr.sictiam.stela.admin.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProvisioningRequest {

    @JsonProperty(value = "instance_id")
    private String instanceId;

    @JsonProperty(value = "client_id")
    private String clientId;

    public ProvisioningRequest() {
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getClientId() {
        return clientId;
    }

    @Override
    public String toString() {
        return "ProvisioningRequest{" +
                "instanceId='" + instanceId + '\'' +
                ", clientId='" + clientId + '\'' +
                '}';
    }
}

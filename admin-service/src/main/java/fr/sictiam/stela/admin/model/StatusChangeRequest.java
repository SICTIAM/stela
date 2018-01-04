package fr.sictiam.stela.admin.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StatusChangeRequest {

    @JsonProperty("instance_id")
    private String instanceId;
    @JsonProperty("status")
    private String status;

    public StatusChangeRequest() {
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getStatus() {
        return status;
    }
}

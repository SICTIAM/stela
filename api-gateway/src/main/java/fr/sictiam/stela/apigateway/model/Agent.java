package fr.sictiam.stela.apigateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.oasis_eu.spring.kernel.model.UserInfo;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class Agent {

    @JsonProperty(value = "family_name")
    private String familyName;
    @JsonProperty(value = "given_name")
    private String givenName;
    // the sub in OpenId Connect parliance
    @NotNull
    @NotEmpty
    private String sub;
    @NotNull
    @NotEmpty
    private String email;
    @NotNull
    private Boolean admin;
    @NotNull
    @NotEmpty
    @JsonProperty(value = "instance_id")
    private String instanceId;

    protected Agent() {
    }

    public Agent(UserInfo userInfo, boolean admin, String instanceId) {
        this.sub = userInfo.getUserId();
        this.familyName = userInfo.getFamilyName();
        this.givenName = userInfo.getGivenName();
        this.email = userInfo.getEmail();
        this.admin = admin;
        this.instanceId = instanceId;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getEmail() {
        return email;
    }

    public String getSub() {
        return sub;
    }

    public boolean isAdmin() {
        return admin;
    }

    public String getInstanceId() {
        return instanceId;
    }
}

package fr.sictiam.stela.apigateway.model;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.oasis_eu.spring.kernel.model.UserInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

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
    @JsonProperty(value = "slug_name")
    private String slugName;
  
    protected Agent() {
    }

    public Agent(UserInfo userInfo, boolean admin, String slugName) {
        this.sub = userInfo.getUserId();
        this.familyName = userInfo.getFamilyName();
        this.givenName = userInfo.getGivenName();
        this.email = userInfo.getEmail();
        this.admin = admin;
        this.slugName = slugName;
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

    public String getSlugName() {
        return slugName;
    }
}

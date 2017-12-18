package fr.sictiam.stela.apigateway.model;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotEmpty;
import org.oasis_eu.spring.kernel.model.UserInfo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.netflix.infix.lang.infix.antlr.EventFilterParser.boolean_expr_return;

public class Agent {

    private String uuid;
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

  
    protected Agent() {
    }

    public Agent(UserInfo userInfo, boolean admin) {
        this.sub = userInfo.getUserId();
        this.familyName = userInfo.getFamilyName();
        this.givenName = userInfo.getGivenName();
        this.email = StringUtils.isNotEmpty(userInfo.getEmail())? userInfo.getEmail(): "emai@email.fr";
        this.admin = admin;
    }

    public String getUuid() {
        return uuid;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

}

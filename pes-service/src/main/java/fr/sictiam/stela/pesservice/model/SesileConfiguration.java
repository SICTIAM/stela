package fr.sictiam.stela.pesservice.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class SesileConfiguration {

    @Id
    private String profileUuid;

    private int visibility;
    // in day
    private int validationLimit;

    private int serviceOrganisationNumber;

    private int type;

    private String token;

    private String secret;

    public SesileConfiguration() {
    }

    public String getProfileUuid() {
        return profileUuid;
    }

    public void setProfileUuid(String profileUuid) {
        this.profileUuid = profileUuid;
    }

    public int getVisibility() {
        return visibility;
    }

    public void setVisibility(int visibility) {
        this.visibility = visibility;
    }

    public int getValidationLimit() {
        return validationLimit;
    }

    public void setValidationLimit(int validationLimit) {
        this.validationLimit = validationLimit;
    }

    public int getServiceOrganisationNumber() {
        return serviceOrganisationNumber;
    }

    public void setServiceOrganisationNumber(int serviceOrganisationNumber) {
        this.serviceOrganisationNumber = serviceOrganisationNumber;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

}

package fr.sictiam.stela.pesservice.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class SesileConfiguration {

    @Id
    private String profileUuid;

    private int visibility;

    private int daysToValidated;

    private int serviceOrganisationNumber;

    private int type;

    private String token;

    private String secret;

    public SesileConfiguration() {
    }

    public SesileConfiguration(String token, String secret) {
        super();
        this.token = token;
        this.secret = secret;
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

    public void setVisibility(Integer visibility) {
        this.visibility = visibility;
    }

    public int getDaysToValidated() {
        return daysToValidated;
    }

    public void setDaysToValidated(Integer daysToValidated) {
        this.daysToValidated = daysToValidated;
    }

    public int getServiceOrganisationNumber() {
        return serviceOrganisationNumber;
    }

    public void setServiceOrganisationNumber(Integer serviceOrganisationNumber) {
        this.serviceOrganisationNumber = serviceOrganisationNumber;
    }

    public int getType() {
        return type;
    }

    public void setType(Integer type) {
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

package fr.sictiam.stela.pesservice.model.ui;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GenericAccount {

    private String uuid;

    private String software;

    private String email;

    private String password;

    private String serial;

    private String vendor;

    private Set<LocalAuthorityGeneric> localAuthorities;

    protected GenericAccount() {
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getSoftware() {
        return software;
    }

    public void setSoftware(String software) {
        this.software = software;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public Set<LocalAuthorityGeneric> getLocalAuthorities() {
        return localAuthorities;
    }

    public void setLocalAuthorities(Set<LocalAuthorityGeneric> localAuthorities) {
        this.localAuthorities = localAuthorities;
    }

}

package fr.sictiam.stela.admin.model.UI;

import java.util.Set;

public class GenericAccountUI {

    private String uuid;
    private String software;
    private String email;
    private String password;
    private String serial;
    private String vendor;
    private Set<String> localAuthoritySirens;

    public GenericAccountUI() {
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

    public Set<String> getLocalAuthoritySirens() {
        return localAuthoritySirens;
    }

    public void setLocalAuthoritySirens(Set<String> localAuthoritySirens) {
        this.localAuthoritySirens = localAuthoritySirens;
    }

}

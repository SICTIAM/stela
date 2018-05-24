package fr.sictiam.stela.acteservice.soap.endpoints;

import java.util.Set;

public class PaullSoapToken {

    private String accountUuid;
    private String siren;

    private Set<String> activatedModules;

    public PaullSoapToken() {
    }

    public String getAccountUuid() {
        return accountUuid;
    }

    public void setAccountUuid(String accountUuid) {
        this.accountUuid = accountUuid;
    }

    public String getSiren() {
        return siren;
    }

    public void setSiren(String siren) {
        this.siren = siren;
    }

    public Set<String> getActivatedModules() {
        return activatedModules;
    }

    public void setActivatedModules(Set<String> activatedModules) {
        this.activatedModules = activatedModules;
    }

}

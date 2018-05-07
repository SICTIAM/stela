package fr.sictiam.stela.admin.soap.model;

import fr.sictiam.stela.admin.model.Module;

import java.util.Set;

public class PaullSoapToken {

    private String accountUuid;
    private String siren;

    private Set<Module> activatedModules;

    public PaullSoapToken(String accountUuid, String siren, Set<Module> activatedModules) {
        super();
        this.accountUuid = accountUuid;
        this.siren = siren;
        this.activatedModules = activatedModules;
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

    public Set<Module> getActivatedModules() {
        return activatedModules;
    }

    public void setActivatedModules(Set<Module> activatedModules) {
        this.activatedModules = activatedModules;
    }

}

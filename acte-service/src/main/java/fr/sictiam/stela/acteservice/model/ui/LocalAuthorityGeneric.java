package fr.sictiam.stela.acteservice.model.ui;

import java.util.Set;

public class LocalAuthorityGeneric {

    private String uuid;
    private String name;
    private String siren;

    private Set<String> activatedModules;

    public LocalAuthorityGeneric() {
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

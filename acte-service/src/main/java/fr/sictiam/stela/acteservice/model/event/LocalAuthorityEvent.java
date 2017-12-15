package fr.sictiam.stela.acteservice.model.event;

import java.util.Set;

import fr.sictiam.stela.acteservice.model.LocalAuthority;
import fr.sictiam.stela.acteservice.model.Profile;
import fr.sictiam.stela.acteservice.model.WorkGroup;

public class LocalAuthorityEvent extends Event {

    private String uuid;
    private String name;
    private String siren;

    private Set<Module> activatedModules;
    
    private Set<WorkGroup> groups;
    
    private Set<Profile> profiles;
    
    public LocalAuthorityEvent() {
        super(LocalAuthorityEvent.class.getName());
    }
    
    public LocalAuthorityEvent(LocalAuthority localAuthority) {
        super(LocalAuthorityEvent.class.getName());
        this.uuid = localAuthority.getUuid();
        this.name = localAuthority.getName();
        this.siren = localAuthority.getSiren();
        this.groups = localAuthority.getGroups();
        this.profiles = localAuthority.getProfiles();
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
    
    public Set<Module> getActivatedModules() {
        return activatedModules;
    }

    public void setActivatedModules(Set<Module> activatedModules) {
        this.activatedModules = activatedModules;
    }    
    
    public Set<WorkGroup> getGroups() {
        return groups;
    }

    public void setGroups(Set<WorkGroup> groups) {
        this.groups = groups;
    }

    public Set<Profile> getProfiles() {
        return profiles;
    }

    public void setProfiles(Set<Profile> profiles) {
        this.profiles = profiles;
    }
}

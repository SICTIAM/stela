package fr.sictiam.stela.admin.model.UI;

import fr.sictiam.stela.admin.model.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class LocalAuthorityUI {

    private String uuid;
    private String name;
    private String siren;
    private Set<Module> activatedModules;
    private Set<WorkGroup> groups;
    private Set<Agent> agents;

    public LocalAuthorityUI(LocalAuthority localAuthority) {
        this.uuid = localAuthority.getUuid();
        this.name = localAuthority.getName();
        this.siren = localAuthority.getSiren();
        this.activatedModules = localAuthority.getActivatedModules();
        this.groups = localAuthority.getGroups();
        this.agents = localAuthority.getProfiles().stream().map(Profile::getAgent).collect(Collectors.toSet());
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getSiren() {
        return siren;
    }

    public Set<Module> getActivatedModules() {
        return activatedModules;
    }

    public Set<WorkGroup> getGroups() {
        return groups;
    }

    public Set<Agent> getAgents() {
        return agents;
    }
}

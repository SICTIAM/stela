package fr.sictiam.stela.admin.model.UI;

import fr.sictiam.stela.admin.model.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

public class LocalAuthorityUI {

    private String uuid;
    private String name;
    private String siren;
    private Set<Module> activatedModules;
    private Set<WorkGroup> groups;
    private Set<Agent> agents;

    public LocalAuthorityUI(String uuid, String name, String siren, Set<Module> activatedModules, Set<WorkGroup> groups, Set<Agent> agents) {
        this.uuid = uuid;
        this.name = name;
        this.siren = siren;
        this.activatedModules = activatedModules;
        this.groups = groups;
        this.agents = agents;
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

package fr.sictiam.stela.admin.model.UI;

import fr.sictiam.stela.admin.model.*;

import java.util.Set;
import java.util.stream.Collectors;

public class LocalAuthorityUI {

    private String uuid;
    private String name;
    private String siren;
    private Set<Module> activatedModules;
    private Set<WorkGroupUI> groups;
    private Set<AgentUI> agents;

    public LocalAuthorityUI(LocalAuthority localAuthority) {
        this.uuid = localAuthority.getUuid();
        this.name = localAuthority.getName();
        this.siren = localAuthority.getSiren();
        this.activatedModules = localAuthority.getActivatedModules();
        this.groups = localAuthority.getGroups().stream().map(WorkGroupUI::new).collect(Collectors.toSet());
        this.agents = localAuthority.getProfiles().stream()
                .map(Profile::getAgent)
                .map(AgentUI::new)
                .collect(Collectors.toSet());
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

    public Set<WorkGroupUI> getGroups() {
        return groups;
    }

    public Set<AgentUI> getAgents() {
        return agents;
    }
}

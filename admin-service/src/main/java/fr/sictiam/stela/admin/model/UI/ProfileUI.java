package fr.sictiam.stela.admin.model.UI;

import fr.sictiam.stela.admin.model.Profile;

import java.util.Set;
import java.util.stream.Collectors;

public class ProfileUI {

    private String uuid;
    private SmallAgentUI agent;
    private SmallLocalAuthorityUI localAuthority;
    private Set<WorkGroupUI> groups;

    public ProfileUI(Profile profile) {
        this.uuid = profile.getUuid();
        this.agent = new SmallAgentUI(profile.getAgent());
        this.localAuthority = new SmallLocalAuthorityUI(profile.getLocalAuthority());
        this.groups = profile.getGroups().stream().map(WorkGroupUI::new).collect(Collectors.toSet());
    }

    public String getUuid() {
        return uuid;
    }

    public SmallAgentUI getAgent() {
        return agent;
    }

    public SmallLocalAuthorityUI getLocalAuthority() {
        return localAuthority;
    }

    public Set<WorkGroupUI> getGroups() {
        return groups;
    }
}

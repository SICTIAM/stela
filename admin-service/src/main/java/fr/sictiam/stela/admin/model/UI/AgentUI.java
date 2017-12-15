package fr.sictiam.stela.admin.model.UI;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.sictiam.stela.admin.model.Agent;

import java.util.Set;
import java.util.stream.Collectors;

public class AgentUI {

    private String uuid;
    @JsonProperty(value = "family_name")
    private String familyName;
    @JsonProperty(value = "given_name")
    private String givenName;
    private String email;
    private Boolean admin;
    private Set<ProfileUI> profiles;

    public AgentUI(Agent agent) {
        this.uuid = agent.getUuid();
        this.familyName = agent.getFamilyName();
        this.givenName = agent.getGivenName();
        this.email = agent.getEmail();
        this.admin = agent.isAdmin();
        this.profiles = agent.getProfiles().stream().map(ProfileUI::new).collect(Collectors.toSet());
    }

    public String getUuid() {
        return uuid;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getEmail() {
        return email;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public Set<ProfileUI> getProfiles() {
        return profiles;
    }
}

package fr.sictiam.stela.admin.model.UI;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.sictiam.stela.admin.model.Agent;

public class SmallAgentUI {

    private String uuid;
    @JsonProperty(value = "family_name")
    private String familyName;
    @JsonProperty(value = "given_name")
    private String givenName;
    private String email;

    public SmallAgentUI(Agent agent) {
        this.uuid = agent.getUuid();
        this.familyName = agent.getFamilyName();
        this.givenName = agent.getGivenName();
        this.email = agent.getEmail();
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
}

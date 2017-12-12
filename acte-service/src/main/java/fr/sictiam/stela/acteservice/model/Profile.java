package fr.sictiam.stela.acteservice.model;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Profile {

    @Id
    private String uuid;

    @ManyToOne
    @JsonIgnore
    private LocalAuthority localAuthority;

    @ManyToOne(cascade = CascadeType.ALL)
    private Agent agent;

    private Boolean admin;

    @ManyToMany(mappedBy = "profiles", cascade = CascadeType.ALL)
    private Set<WorkGroup> groups;

    public Profile() {
    }

    public Profile(String uuid, LocalAuthority localAuthority, Agent agent, Boolean admin) {
        this.uuid = uuid;
        this.localAuthority = localAuthority;
        this.agent = agent;
        this.admin = admin;
    }

    public Profile(String uuid, Agent agent, Boolean admin) {
        this.uuid = uuid;
        this.agent = agent;
        this.admin = admin;
    }

    public LocalAuthority getLocalAuthority() {
        return localAuthority;
    }

    public void setLocalAuthority(LocalAuthority localAuthority) {
        this.localAuthority = localAuthority;
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public Set<WorkGroup> getGroups() {
        return groups;
    }

    public void setGroups(Set<WorkGroup> groups) {
        this.groups = groups;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}

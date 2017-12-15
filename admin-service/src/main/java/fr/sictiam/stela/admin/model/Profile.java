package fr.sictiam.stela.admin.model;

import java.util.Set;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

@Entity
public class Profile {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name="profile_uuid")
    private String uuid;
    
    @ManyToOne
    @JsonIgnore
    private LocalAuthority localAuthority;
    
    @ManyToOne
    @JsonIgnore
    private Agent agent;
    
    private Boolean admin;
    
    @ManyToMany(mappedBy="profiles")
    @JsonIgnore
    private Set<WorkGroup> groups;
    
    public Profile() {
    }
    
    
    public Profile(LocalAuthority localAuthority, Agent agent, Boolean admin) {
        super();
        this.localAuthority = localAuthority;
        this.agent = agent;
        this.admin = admin;
    }

    public String getUuid() {
        return uuid;
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
}

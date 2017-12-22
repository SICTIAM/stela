package fr.sictiam.stela.admin.model;

import java.util.Set;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import fr.sictiam.stela.admin.model.UI.Views;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Profile {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JsonView(Views.ProfileViewPublic.class)
    private String uuid;
    
    @ManyToOne(fetch=FetchType.EAGER)
    @JsonView(Views.ProfileViewPrivate.class)
    private LocalAuthority localAuthority;
    
    @ManyToOne
    @JsonView(Views.ProfileViewPublic.class)
    private Agent agent;
    
    @JsonView(Views.ProfileViewPublic.class)
    private Boolean admin;
    
    @ManyToMany(mappedBy="profiles")
    @JsonView(Views.ProfileViewPrivate.class)
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

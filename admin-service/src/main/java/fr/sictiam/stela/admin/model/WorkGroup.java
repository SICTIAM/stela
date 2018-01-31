package fr.sictiam.stela.admin.model;

import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonView;

import fr.sictiam.stela.admin.model.UI.Views;
import fr.sictiam.stela.admin.model.UI.Views.WorkGroupViewChain;
import fr.sictiam.stela.admin.model.UI.Views.WorkGroupViewPublic;

//Group is reserved in Postgresql
@Entity
public class WorkGroup {
    
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JsonView(WorkGroupViewPublic.class)
    private String uuid;
    
    @ManyToOne
    @JsonView(Views.WorkGroupViewPrivate.class)
    private LocalAuthority localAuthority;
    
    @ManyToMany(targetEntity=Profile.class)
    @JoinTable(name = "group_to_profile")
    @JsonView(WorkGroupViewChain.class)
    private Set<Profile> profiles;
    
    @JsonView(WorkGroupViewPublic.class)
    private String name;    
    
    @ElementCollection(fetch = FetchType.EAGER)
    @JsonView(Views.WorkGroupViewPublic.class)
    private Set<String> rights; 

    public WorkGroup() {
        
    }
    
    public WorkGroup(LocalAuthority localAuthority, String name) {
        super();
        this.localAuthority = localAuthority;
        this.name = name;
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

    public Set<Profile> getProfiles() {
        return profiles;
    }

    public void setProfiles(Set<Profile> profiles) {
        this.profiles = profiles;
    }
    
    public Set<String> getRights() {
        return rights;
    }

    public void setRights(Set<String> rights) {
        this.rights = rights;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}

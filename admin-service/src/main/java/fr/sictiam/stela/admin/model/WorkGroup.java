package fr.sictiam.stela.admin.model;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.GenericGenerator;

//Group is reserved in Postgresql
@Entity
public class WorkGroup {
    
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
    
    @ManyToOne
    private LocalAuthority localAuthority;
    
    @ManyToMany(targetEntity=Profile.class)
    @JoinTable(name = "group_to_profile")
    private Set<Profile> profiles;
    
    private String name;    

    public WorkGroup() {
        
    }
    
    public WorkGroup(LocalAuthority localAuthority, String name) {
        super();
        this.localAuthority = localAuthority;
        this.name = name;
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
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    
}

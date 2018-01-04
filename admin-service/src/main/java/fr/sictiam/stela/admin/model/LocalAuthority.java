package fr.sictiam.stela.admin.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import fr.sictiam.stela.admin.model.UI.Views;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class LocalAuthority {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JsonView(Views.LocalAuthorityViewPublic.class)
    private String uuid;
    @JsonView(Views.LocalAuthorityViewPublic.class)
    private String name;
    @Column(unique = true)
    @JsonView(Views.LocalAuthorityViewPublic.class)
    private String siren;
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @JsonView(Views.LocalAuthorityViewPublic.class)
    private Set<Module> activatedModules;   

    @JsonIgnore
    private OzwilloInstanceInfo ozwilloInstanceInfo;
    
    @OneToMany(mappedBy = "localAuthority", fetch = FetchType.EAGER)
    @JsonView(Views.LocalAuthorityViewPrivate.class)
    private Set<WorkGroup> groups;
    
    @OneToMany(mappedBy = "localAuthority", fetch = FetchType.EAGER)
    @JsonView(Views.LocalAuthorityViewPrivate.class)
    private Set<Profile> profiles;
    
    protected LocalAuthority() {
        this.activatedModules = new HashSet<>();
    }

    public LocalAuthority(String name, String siren) {
        this.name = name;
        this.siren = siren;
        this.activatedModules = new HashSet<>();
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSiren() {
        return siren;
    }

    public void setSiren(String siren) {
        this.siren = siren;
    }

    public Set<Module> getActivatedModules() {
        return activatedModules;
    }

    public void setActivatedModules(Set<Module> activatedModules) {
        this.activatedModules = activatedModules;
    }

    public void addModule(Module module) {
        this.activatedModules.add(module);
    }

    public void removeModule(Module module) {
        this.activatedModules.remove(module);
    }

    public OzwilloInstanceInfo getOzwilloInstanceInfo() {
        return ozwilloInstanceInfo;
    }

    public void setOzwilloInstanceInfo(OzwilloInstanceInfo ozwilloInstanceInfo) {
        this.ozwilloInstanceInfo = ozwilloInstanceInfo;
    }
    
    public Set<WorkGroup> getGroups() {
        return groups;
    }

    public void setGroups(Set<WorkGroup> groups) {
        this.groups = groups;
    }

    public Set<Profile> getProfiles() {
        return profiles;
    }

    public void setProfiles(Set<Profile> profiles) {
        this.profiles = profiles;
    }
    
    @Override
    public String toString() {
        return "LocalAuthority{" +
                "uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", siren='" + siren + '\'' +
                '}';
    }
}

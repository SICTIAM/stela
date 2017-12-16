package fr.sictiam.stela.acteservice.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

//Group is reserved in Postgresql
@Entity
public class WorkGroup {

    @Id
    private String uuid;

    @ManyToOne
    @JsonIgnore
    private LocalAuthority localAuthority;

    @ManyToMany(targetEntity = Profile.class, cascade = CascadeType.ALL)
    @JoinTable(name = "group_to_profile")
    private Set<Profile> profiles;

    private String name;

    public WorkGroup() {

    }

    public WorkGroup(String uuid, LocalAuthority localAuthority, String name) {
        this.uuid = uuid;
        this.localAuthority = localAuthority;
        this.name = name;
    }

    public WorkGroup(String uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public LocalAuthority getLocalAuthority() {
        return localAuthority;
    }

    public void setLocalAuthority(LocalAuthority localAuthority) {
        this.localAuthority = localAuthority;
    }

    public Set<Profile> getProfiles() {
        return profiles == null ? new HashSet<>() : profiles;
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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

}

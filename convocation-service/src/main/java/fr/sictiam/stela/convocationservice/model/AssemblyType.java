package fr.sictiam.stela.convocationservice.model;

import com.fasterxml.jackson.annotation.JsonView;
import fr.sictiam.stela.convocationservice.model.ui.Views;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

import java.util.Set;

@Entity
public class AssemblyType {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JsonView(Views.AssemblyTypeViewPublic.class)
    private String uuid;

    @JsonView(Views.AssemblyTypeViewPublic.class)
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "profile_uuids", joinColumns = @JoinColumn(name = "assembly_type_uuid"))
    @Column(name = "profile_uuid")
    @JsonView(Views.AssemblyTypeViewPublic.class)
    private Set<String> profileUuids;

    @OneToMany(fetch = FetchType.EAGER)
    @JsonView(Views.AssemblyTypeViewPrivate.class)
    private Set<ExternalUser> externalUsers;

    public AssemblyType(String name, Set<String> profileUuids, Set<ExternalUser> externalUsers,
            LocalAuthority localAuthority) {
        this.name = name;
        this.profileUuids = profileUuids;
        this.externalUsers = externalUsers;
        this.localAuthority = localAuthority;
    }

    @ManyToOne
    @JsonView(Views.AssemblyTypeViewPrivate.class)
    private LocalAuthority localAuthority;

    public AssemblyType() {
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

    public Set<String> getProfileUuids() {
        return profileUuids;
    }

    public void setProfileUuids(Set<String> profileUuids) {
        this.profileUuids = profileUuids;
    }

    public Set<ExternalUser> getExternalUsers() {
        return externalUsers;
    }

    public void setExternalUsers(Set<ExternalUser> externalUsers) {
        this.externalUsers = externalUsers;
    }

    public LocalAuthority getLocalAuthority() {
        return localAuthority;
    }

    public void setLocalAuthority(LocalAuthority localAuthority) {
        this.localAuthority = localAuthority;
    }

}

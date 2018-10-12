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
    @JsonView(Views.LocalAuthorityViewBasic.class)
    private String uuid;
    @JsonView(Views.LocalAuthorityViewBasic.class)
    private String name;
    // the slug name used in local authority's domain name, eg <valbonne>.stela.fr
    @JsonView(Views.LocalAuthorityViewBasic.class)
    @Column(unique = true)
    private String slugName;
    @Column(unique = true)
    @JsonView(Views.LocalAuthorityViewBasic.class)
    private String siren;
    @Enumerated(EnumType.STRING)
    @Column(length = 12)
    private Status status;
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

    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "local_authority_certificate", inverseJoinColumns = { @JoinColumn(name = "certificate_uuid") })
    @JsonView(Views.LocalAuthorityViewPrivate.class)
    private Set<Certificate> certificates;

    protected LocalAuthority() {
        this.activatedModules = new HashSet<>();
    }

    public LocalAuthority(String uuid) {
        this.uuid = uuid;
        this.activatedModules = new HashSet<>();
    }

    public LocalAuthority(String name, String siren, String slugName) {
        this.name = name;
        this.siren = siren;
        this.slugName = slugName;
        this.status = Status.RUNNING;
        this.activatedModules = new HashSet<>();
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getSlugName() {
        return slugName;
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
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
        return groups == null ? new HashSet<>() : groups;
    }

    public void setGroups(Set<WorkGroup> groups) {
        this.groups = groups;
    }

    public Set<Profile> getProfiles() {
        return profiles == null ? new HashSet<>() : profiles;
    }

    public void setProfiles(Set<Profile> profiles) {
        this.profiles = profiles;
    }

    public Set<Certificate> getCertificates() {
        return certificates;
    }

    public void setCertificates(Set<Certificate> certificates) {
        this.certificates = certificates;
    }

    @Override
    public String toString() {
        return "LocalAuthority{" + "uuid='" + uuid + '\'' + ", name='" + name + '\'' + ", siren='" + siren + '\'' + '}';
    }

    public enum Status {
        RUNNING, STOPPED
    }
}

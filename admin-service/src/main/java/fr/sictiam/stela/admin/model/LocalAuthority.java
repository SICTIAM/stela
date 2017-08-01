package fr.sictiam.stela.admin.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class LocalAuthority {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
    private String name;
    @Column(unique = true)
    private String siren;
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Module> activatedModules;
    private OzwilloInstanceInfo ozwilloInstanceInfo;

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

    @Override
    public String toString() {
        return "LocalAuthority{" +
                "uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", siren='" + siren + '\'' +
                '}';
    }
}

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
    private String siren;
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Module> modules;

    protected LocalAuthority() {
        this.modules = new HashSet<>();
    }

    public LocalAuthority(String name, String siren) {
        this.name = name;
        this.siren = siren;
        this.modules = new HashSet<>();
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

    public Set<Module> getModules() {
        return modules;
    }

    public void setModules(Set<Module> modules) {
        this.modules = modules;
    }

    public void addModule(Module module) {
        this.modules.add(module);
    }

    public void removeModule(Module module) {
        this.modules.remove(module);
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

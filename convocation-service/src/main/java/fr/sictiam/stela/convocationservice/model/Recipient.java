package fr.sictiam.stela.convocationservice.model;

import com.fasterxml.jackson.annotation.JsonView;
import fr.sictiam.stela.convocationservice.model.ui.Views;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;

import java.util.Set;

@Entity
public class Recipient {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JsonView(Views.UserViewPublic.class)
    private String uuid;

    @JsonView(Views.UserViewPublic.class)
    private String firstname;

    @JsonView(Views.UserViewPublic.class)
    private String lastname;

    @JsonView(Views.UserViewPublic.class)
    private String email;

    @JsonView(Views.UserViewPublic.class)
    private boolean active = true;

    @JsonView(Views.UserViewPrivate.class)
    private String token;

    @OneToOne(fetch = FetchType.EAGER)
    @JsonView(Views.UserViewPublic.class)
    private LocalAuthority localAuthority;

    @JsonView(Views.UserViewPublic.class)
    @ManyToMany(mappedBy = "recipients")
    private Set<AssemblyType> assemblyTypes;

    public Recipient() {
    }

    public Recipient(String firstname, String lastname, String email, LocalAuthority localAuthority) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.localAuthority = localAuthority;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUuid() {
        return uuid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalAuthority getLocalAuthority() {
        return localAuthority;
    }

    public void setLocalAuthority(LocalAuthority localAuthority) {
        this.localAuthority = localAuthority;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Set<AssemblyType> getAssemblyTypes() {
        return assemblyTypes;
    }

    public void setAssemblyTypes(Set<AssemblyType> assemblyTypes) {
        this.assemblyTypes = assemblyTypes;
    }
}

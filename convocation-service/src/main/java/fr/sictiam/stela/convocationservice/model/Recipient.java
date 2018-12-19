package fr.sictiam.stela.convocationservice.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fr.sictiam.stela.convocationservice.config.LocalDateTimeDeserializer;
import fr.sictiam.stela.convocationservice.config.LocalDateTimeSerializer;
import fr.sictiam.stela.convocationservice.model.ui.Views;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import java.time.LocalDateTime;
import java.util.Set;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "uuid")
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
    private String phoneNumber;

    @JsonView(Views.UserViewPublic.class)
    private Boolean active;

    @JsonIgnore
    @JsonView(Views.UserViewPrivate.class)
    private String token;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonView(Views.UserViewPrivate.class)
    private LocalAuthority localAuthority;

    @JsonView(Views.UserViewPublic.class)
    @ManyToMany(mappedBy = "recipients", fetch = FetchType.EAGER)
    private Set<AssemblyType> assemblyTypes;

    @JsonView(Views.UserViewPublic.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    LocalDateTime inactivityDate;

    public Recipient() {
    }

    public Recipient(String firstname, String lastname, String email, String phoneNumber,
            LocalAuthority localAuthority) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.phoneNumber = phoneNumber;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Set<AssemblyType> getAssemblyTypes() {
        return assemblyTypes;
    }

    public void setAssemblyTypes(Set<AssemblyType> assemblyTypes) {
        this.assemblyTypes = assemblyTypes;
    }

    public LocalDateTime getInactivityDate() {
        return inactivityDate;
    }

    public void setInactivityDate(LocalDateTime inactivityDate) {
        this.inactivityDate = inactivityDate;
    }
}

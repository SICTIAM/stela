package fr.sictiam.stela.convocationservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fr.sictiam.stela.convocationservice.config.LocalDateTimeDeserializer;
import fr.sictiam.stela.convocationservice.config.LocalDateTimeSerializer;
import fr.sictiam.stela.convocationservice.model.csv.RecipientBean;
import fr.sictiam.stela.convocationservice.model.ui.Views;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.GenericGenerator;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
public class Recipient implements Comparable<Recipient> {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JsonView(Views.Public.class)
    private String uuid;

    @JsonView(Views.Public.class)
    private String firstname;

    @JsonView(Views.Public.class)
    private String lastname;

    @JsonView(Views.Public.class)
    private String email;

    @JsonView(Views.Recipient.class)
    private String phoneNumber;

    @JsonView(Views.Public.class)
    private Boolean active;

    @JsonView(Views.Public.class)
    private Boolean guest = false;

    @JsonIgnore
    @JsonView(Views.RecipientPrivate.class)
    private String token;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonView(Views.RecipientInternal.class)
    private LocalAuthority localAuthority;

    @JsonView(Views.RecipientInternal.class)
    @ManyToMany(mappedBy = "recipients", fetch = FetchType.EAGER)
    private Set<AssemblyType> assemblyTypes;

    @JsonView(Views.Recipient.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime inactivityDate;

    @JsonView(Views.Recipient.class)
    private boolean serviceAssemblee = false;

    @JsonView(Views.Public.class)
    private String epciName;

    public Recipient() {
    }

    public Recipient(String firstname, String lastname, String email, String phoneNumber,
            LocalAuthority localAuthority, String epciName) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.localAuthority = localAuthority;
        this.epciName = epciName;
    }

    public Recipient(RecipientBean bean) {
        this(bean.getFirstname(), bean.getLastname(), bean.getEmail(), bean.getPhoneNumber(), null, bean.getEpci());
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

    public Boolean getActive() {
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

    public Boolean isGuest() {
        return guest;
    }

    public void setGuest(Boolean guest) {
        this.guest = guest;
    }

    public String getFullName() {
        return StringUtils.capitalize(firstname.toLowerCase()) + " " +
                StringUtils.capitalize(lastname.toLowerCase()) +
                (StringUtils.isNotBlank(epciName) ? " (" + epciName + ")" : "");
    }

    public boolean isServiceAssemblee() {
        return serviceAssemblee;
    }

    public void setServiceAssemblee(boolean serviceAssemblee) {
        this.serviceAssemblee = serviceAssemblee;
    }

    public String getEpciName() {
        return epciName;
    }

    public void setEpciName(String epciName) {
        this.epciName = epciName;
    }

    @Override public String toString() {
        return '{' +
                "\"uuid\": \"" + uuid + "\"" +
                ",\"firstname\": \"" + firstname + "\"" +
                ",\"lastname\": \"" + lastname + "\"" +
                ",\"email\": \"" + email + "\"" +
                ",\"phoneNumber\": \"" + phoneNumber + "\"" +
                ",\"active\": " + active +
                ",\"token\": \"" + token + "\"" +
                ",\"guest\": " + guest +
                ",\"epciName\": \"" + epciName + "\"" +
                '}';
    }

    @Override public int compareTo(@NotNull Recipient recipient) {
        String r1 = lastname + firstname + email;
        String r2 = recipient.getLastname() + getFirstname() + getEmail();

        return r1.compareTo(r2);
    }

    @Override public boolean equals(Object o) {
        return uuid.equals(((Recipient) o).getUuid());
    }
}

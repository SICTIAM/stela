package fr.sictiam.stela.admin.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.Set;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
public class Agent {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
    @JsonProperty(value = "family_name")
    private String familyName;
    @JsonProperty(value = "given_name")
    private String givenName;
    // the sub in OpenId Connect parliance
    @NotNull
    @NotEmpty
    @Column(unique = true)
    @JsonIgnore
    private String sub;
    @NotNull
    @NotEmpty
    private String email;
    @NotNull
    private Boolean admin;
    
    @OneToMany(mappedBy = "agent")
    private Set<Profile> profiles;
    
    protected Agent() {
    }

    public Agent(String familyName, String givenName, String email) {
        this.familyName = familyName;
        this.givenName = givenName;
        this.email = email;
    }

    public String getUuid() {
        return uuid;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }
    
    public Set<Profile> getProfiles() {
        return profiles;
    }

    public void setProfiles(Set<Profile> profiles) {
        this.profiles = profiles;
    }
    
    @Override
    public String toString() {
        return "Agent{" +
                "uuid='" + uuid + '\'' +
                ", familyName='" + familyName + '\'' +
                ", givenName='" + givenName + '\'' +
                ", sub='" + sub + '\'' +
                ", email='" + email + '\'' +
                ", admin=" + admin +
                '}';
    }
}

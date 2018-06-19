package fr.sictiam.stela.admin.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import fr.sictiam.stela.admin.model.UI.Views;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Agent {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JsonView(Views.AgentViewPublic.class)
    private String uuid;
    @JsonProperty(value = "family_name")
    @JsonView(Views.AgentViewPublic.class)
    private String familyName;
    @JsonProperty(value = "given_name")
    @JsonView(Views.AgentViewPublic.class)
    private String givenName;
    // the sub in OpenId Connect parliance
    @Column(unique = true)
    @JsonView(Views.AgentViewPrivate.class)
    private String sub;
    @NotNull
    @NotEmpty
    @JsonView(Views.AgentViewPublic.class)
    private String email;
    @NotNull
    @JsonView(Views.AgentViewPublic.class)
    private Boolean admin;
    @Transient
    @JsonProperty(value = "slug_name")
    private String slugName;

    @OneToMany(mappedBy = "agent", fetch = FetchType.EAGER)
    @JsonView(Views.AgentViewPrivate.class)
    private Set<Profile> profiles;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonView(Views.AgentViewPublic.class)
    private Certificate certificate;

    private Boolean imported;

    protected Agent() {
    }

    public Agent(String email) {
        this.email = email;
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

    public String getSlugName() {
        return slugName;
    }

    public void setSlugName(String slugName) {
        this.slugName = slugName;
    }

    public Set<Profile> getProfiles() {
        return profiles != null ? profiles : new HashSet<>();
    }

    public void setProfiles(Set<Profile> profiles) {
        this.profiles = profiles;
    }

    public Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(Certificate certificate) {
        this.certificate = certificate;
    }

    public Boolean getImported() {
        return imported;
    }

    public void setImported(Boolean imported) {
        this.imported = imported;
    }

    @Override
    public String toString() {
        return "Agent{" + "uuid='" + uuid + '\'' + ", familyName='" + familyName + '\'' + ", givenName='" + givenName
                + '\'' + ", sub='" + sub + '\'' + ", email='" + email + '\'' + ", admin=" + admin + '}';
    }
}

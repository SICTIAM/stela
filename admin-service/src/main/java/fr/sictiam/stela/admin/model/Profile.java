package fr.sictiam.stela.admin.model;

import com.fasterxml.jackson.annotation.JsonView;
import fr.sictiam.stela.admin.model.UI.Views;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Profile {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JsonView(Views.ProfileViewPublic.class)
    private String uuid;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonView(Views.ProfileViewPrivate.class)
    private LocalAuthority localAuthority;

    @ManyToOne
    @JsonView(Views.ProfileViewChain.class)
    private Agent agent;

    @JsonView(Views.ProfileViewPublic.class)
    private Boolean admin;

    @ManyToMany(mappedBy = "profiles")
    @JsonView(Views.ProfileViewPrivate.class)
    private Set<WorkGroup> groups;

    @JsonView(Views.ProfileViewPublic.class)
    private String email;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_uuid")
    @JsonView(Views.ProfileViewPublic.class)
    private List<NotificationValue> notificationValues;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @JsonView(Views.ProfileViewPublic.class)
    private Set<Module> localAuthorityNotifications;

    public Profile() {
    }

    public Profile(LocalAuthority localAuthority, Agent agent, Boolean admin) {
        super();
        this.localAuthority = localAuthority;
        this.agent = agent;
        this.admin = admin;
    }

    public String getUuid() {
        return uuid;
    }

    public LocalAuthority getLocalAuthority() {
        return localAuthority;
    }

    public void setLocalAuthority(LocalAuthority localAuthority) {
        this.localAuthority = localAuthority;
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public Set<WorkGroup> getGroups() {
        return groups != null ? groups : new HashSet<>();
    }

    public void setGroups(Set<WorkGroup> groups) {
        this.groups = groups;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<NotificationValue> getNotificationValues() {
        return notificationValues != null ? notificationValues : new ArrayList<>();
    }

    public void setNotificationValues(List<NotificationValue> notificationValues) {
        getNotificationValues().clear();
        getNotificationValues().addAll(notificationValues);
    }

    public Set<Module> getLocalAuthorityNotifications() {
        return localAuthorityNotifications;
    }

    public void setLocalAuthorityNotifications(Set<Module> localAuthorityNotifications) {
        this.localAuthorityNotifications = localAuthorityNotifications;
    }
}

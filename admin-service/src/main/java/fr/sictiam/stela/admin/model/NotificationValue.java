package fr.sictiam.stela.admin.model;

import com.fasterxml.jackson.annotation.JsonView;
import fr.sictiam.stela.admin.model.UI.Views;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class NotificationValue {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JsonView(Views.ProfileViewPublic.class)
    private String uuid;
    @JsonView(Views.ProfileViewPublic.class)
    private String name;
    @JsonView(Views.ProfileViewPublic.class)
    private boolean active;
    @Column(name = "profile_uuid")
    private String profileUuid;

    public NotificationValue() {
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }

    public String getProfileUuid() {
        return profileUuid;
    }

    public void setProfileUuid(String profileUuid) {
        this.profileUuid = profileUuid;
    }
}

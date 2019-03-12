package fr.sictiam.stela.convocationservice.model;

import com.fasterxml.jackson.annotation.JsonView;
import fr.sictiam.stela.convocationservice.model.ui.Views;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;

import java.util.List;

public class Profile {

    @JsonView(Views.Convocation.class)
    private String uuid;

    @NotNull
    @JsonView(Views.Convocation.class)
    private String firstname;

    @NotNull
    @JsonView(Views.Convocation.class)
    private String lastname;

    @NotNull
    @JsonView(Views.Convocation.class)
    private String email;

    private List<NotificationValue> notificationValues;

    public Profile() {
    }

    public Profile(String uuid, String firstname, String lastname, String email, List<NotificationValue> notificationValues) {
        this.uuid = uuid;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.notificationValues = notificationValues;
    }

    public String getUuid() {
        return uuid;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<NotificationValue> getNotificationValues() {
        return notificationValues;
    }

    public void setNotificationValues(List<NotificationValue> notificationValues) {
        this.notificationValues = notificationValues;
    }

    public String getFullName() {
        return StringUtils.capitalize(firstname.toLowerCase()) + " " + StringUtils.capitalize(lastname.toLowerCase());
    }

    @Override public boolean equals(Object o) {
        return uuid.equals(((Profile) o).getUuid());
    }
}

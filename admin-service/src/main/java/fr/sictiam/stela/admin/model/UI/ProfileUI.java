package fr.sictiam.stela.admin.model.UI;

import fr.sictiam.stela.admin.model.Notification;

import java.util.Set;

public class ProfileUI {

    private String uuid;
    private String email;
    private Set<Notification> notifications;

    public ProfileUI() {
    }

    public String getUuid() {
        return uuid;
    }

    public String getEmail() {
        return email;
    }

    public Set<Notification> getNotifications() {
        return notifications;
    }
}

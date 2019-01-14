package fr.sictiam.stela.admin.model.UI;

import fr.sictiam.stela.admin.model.Module;
import fr.sictiam.stela.admin.model.NotificationValue;

import java.util.Set;

public class ProfileUI {

    private String uuid;
    private String email;
    private Set<NotificationValue> notificationValues;
    private Set<Module> localAuthorityNotifications;

    public ProfileUI() {
    }

    public String getUuid() {
        return uuid;
    }

    public String getEmail() {
        return email;
    }

    public Set<NotificationValue> getNotificationValues() {
        return notificationValues;
    }

    public Set<Module> getLocalAuthorityNotifications() {
        return localAuthorityNotifications;
    }
}

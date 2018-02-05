package fr.sictiam.stela.admin.model.UI;

import fr.sictiam.stela.admin.model.Module;
import fr.sictiam.stela.admin.model.NotificationValue;

import java.util.List;
import java.util.Set;

public class ProfileUI {

    private String uuid;
    private String email;
    private List<NotificationValue> notificationValues;
    private Set<Module> localAuthorityNotifications; 

    public ProfileUI() {
    }

    public String getUuid() {
        return uuid;
    }

    public String getEmail() {
        return email;
    }

    public List<NotificationValue> getNotificationValues() {
        return notificationValues;
    }

    public Set<Module> getLocalAuthorityNotifications() {
        return localAuthorityNotifications;
    }
}

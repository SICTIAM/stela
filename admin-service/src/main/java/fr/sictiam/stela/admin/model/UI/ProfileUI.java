package fr.sictiam.stela.admin.model.UI;

import fr.sictiam.stela.admin.model.NotificationValue;

import java.util.List;

public class ProfileUI {

    private String uuid;
    private String email;
    private List<NotificationValue> notificationValues;

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
}

package fr.sictiam.stela.convocationservice.model;

import java.util.Arrays;
import java.util.List;

public class Notification {

    private NotificationType type;
    private boolean deactivatable;
    private boolean defaultValue;

    public static List<Notification> notifications = Arrays.asList(
            new Notification(NotificationType.CONVOCATION_READ, true, false),
            new Notification(NotificationType.CONVOCATION_RESPONSE, true, false),
            new Notification(NotificationType.NO_RESPONSE_INFO, true, false));

    private Notification(NotificationType type, boolean deactivatable, boolean defaultValue) {
        this.type = type;
        this.deactivatable = deactivatable;
        this.defaultValue = defaultValue;
    }

    public NotificationType getType() {
        return type;
    }

    public boolean isDeactivatable() {
        return deactivatable;
    }

    public boolean isDefaultValue() {
        return defaultValue;
    }
}

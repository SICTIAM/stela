package fr.sictiam.stela.convocationservice.model;

import java.util.Arrays;
import java.util.List;

public class Notification {

    private StatusType statusType;
    private boolean deactivatable;
    private boolean defaultValue;

    public static List<Notification> notifications = Arrays.asList(new Notification(StatusType.SENT, false, true));

    private Notification(StatusType statusType, boolean deactivatable, boolean defaultValue) {
        this.statusType = statusType;
        this.deactivatable = deactivatable;
        this.defaultValue = defaultValue;
    }

    public StatusType getStatusType() {
        return statusType;
    }

    public boolean isDeactivatable() {
        return deactivatable;
    }

    public boolean isDefaultValue() {
        return defaultValue;
    }
}

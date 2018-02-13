package fr.sictiam.stela.acteservice.model;

import java.util.Arrays;
import java.util.List;

import static fr.sictiam.stela.acteservice.model.StatusType.ACK_RECEIVED;
import static fr.sictiam.stela.acteservice.model.StatusType.CANCELLED;
import static fr.sictiam.stela.acteservice.model.StatusType.NACK_RECEIVED;
import static fr.sictiam.stela.acteservice.model.StatusType.SENT;

public class Notification {

    private StatusType statusType;
    private boolean deactivatable;
    private boolean defaultValue;

    public static List<Notification> notifications = Arrays.asList(new Notification(ACK_RECEIVED, false, true),
            new Notification(SENT, true, false), new Notification(CANCELLED, true, false),
            new Notification(NACK_RECEIVED, true, true));

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

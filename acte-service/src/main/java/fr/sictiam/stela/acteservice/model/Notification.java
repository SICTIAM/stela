package fr.sictiam.stela.acteservice.model;

import java.util.Arrays;
import java.util.List;

public class Notification {

    public enum Type {
        SENT("SENT"),
        ACK_RECEIVED("ACK_RECEIVED"),
        NACK_RECEIVED("NACK_RECEIVED"),
        ANOMALIES("ANOMALIES"),
        CANCELLED("CANCELLED");

        final String name;

        Type(String s) {
            name = s;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    private Type type;
    private boolean deactivatable;
    private boolean defaultValue;
    private boolean notificationStatus;

    public static List<Notification> notifications = Arrays.asList(
            new Notification(Type.ACK_RECEIVED, false, true, true),
            new Notification(Type.SENT, true, false, true),
            new Notification(Type.CANCELLED, true, false, true),
            new Notification(Type.ANOMALIES, true, true, false),
            new Notification(Type.NACK_RECEIVED, true, true, false)
    );

    private Notification(Type type, boolean deactivatable, boolean defaultValue, boolean notificationStatus) {
        this.type = type;
        this.deactivatable = deactivatable;
        this.defaultValue = defaultValue;
        this.notificationStatus = notificationStatus;
    }

    public Type getType() {
        return type;
    }

    public boolean isDeactivatable() {
        return deactivatable;
    }

    public boolean isDefaultValue() {
        return defaultValue;
    }

    public boolean isNotificationStatus() {
        return notificationStatus;
    }
}

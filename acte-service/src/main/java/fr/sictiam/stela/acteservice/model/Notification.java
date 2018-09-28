package fr.sictiam.stela.acteservice.model;

import java.util.Arrays;
import java.util.List;

import static fr.sictiam.stela.acteservice.model.StatusType.ACK_RECEIVED;
import static fr.sictiam.stela.acteservice.model.StatusType.CANCELLED;
import static fr.sictiam.stela.acteservice.model.StatusType.NACK_RECEIVED;
import static fr.sictiam.stela.acteservice.model.StatusType.SENT;

public class Notification {

    public enum Type {
        SENT("SENT"),
        ACK_RECEIVED("ACK_RECEIVED"),
        NACK_RECEIVED("NACK_RECEIVED"),
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

    public static List<Notification> notifications = Arrays.asList(
            new Notification(Type.ACK_RECEIVED, false, true),
            new Notification(Type.SENT, true, false),
            new Notification(Type.CANCELLED, true, false),
            new Notification(Type.NACK_RECEIVED, true, true)
    );

    private Notification(Type type, boolean deactivatable, boolean defaultValue) {
        this.type = type;
        this.deactivatable = deactivatable;
        this.defaultValue = defaultValue;
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
}

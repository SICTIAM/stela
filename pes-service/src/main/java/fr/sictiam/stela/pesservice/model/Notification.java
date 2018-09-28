package fr.sictiam.stela.pesservice.model;

import java.util.Arrays;
import java.util.List;

public class Notification {

    public enum Type {
        SENT("SENT"),
        ACK_RECEIVED("ACK_RECEIVED"),
        DAILY_ERRORS("DAILY_ERRORS");

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
            new Notification(Type.SENT, true, false),
            new Notification(Type.ACK_RECEIVED, true, true),
            new Notification(Type.DAILY_ERRORS, true, false)
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


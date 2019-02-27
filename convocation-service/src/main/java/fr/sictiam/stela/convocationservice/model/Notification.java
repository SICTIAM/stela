package fr.sictiam.stela.convocationservice.model;

import java.util.Arrays;
import java.util.List;

public class Notification {

    private HistoryType historyType;
    private boolean deactivatable;
    private boolean defaultValue;

    public static List<Notification> notifications = Arrays.asList(new Notification(HistoryType.SENT, false, true));

    private Notification(HistoryType statusType, boolean deactivatable, boolean defaultValue) {
        historyType = statusType;
        this.deactivatable = deactivatable;
        this.defaultValue = defaultValue;
    }

    public HistoryType getHistoryType() {
        return historyType;
    }

    public boolean isDeactivatable() {
        return deactivatable;
    }

    public boolean isDefaultValue() {
        return defaultValue;
    }
}

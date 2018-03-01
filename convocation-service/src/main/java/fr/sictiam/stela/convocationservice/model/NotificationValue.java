package fr.sictiam.stela.convocationservice.model;

public class NotificationValue {

    private String uuid;
    private String name;
    private boolean active;

    public NotificationValue() {
    }

    public NotificationValue(String uuid, String name, boolean active) {
        this.uuid = uuid;
        this.name = name;
        this.active = active;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }
}

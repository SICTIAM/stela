package fr.sictiam.stela.convocationservice.model;

public enum StatusType {
    CREATED("CREATED"),
    SENT("SENT"),
    GROUP_NOTIFICATION_SENT("GROUP_NOTIFICATION_SENT"),
    NOTIFICATION_SENT("NOTIFICATION_SENT");

    final String name;

    StatusType(String s) {
        name = s;
    }

    @Override
    public String toString() {
        return this.name;
    }
}

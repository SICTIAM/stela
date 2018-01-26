package fr.sictiam.stela.pesservice.model;

public enum StatusType {
    CREATED ("CREATED"),
    SENT ("SENT"),
    NOT_SENT ("NOT_SENT"),
    ACK_RECEIVED ("ACK_RECEIVED"),
    RETURN_RECEIVED("RETURN_RECEIVED"),
    MAX_RETRY_REACH("MAX_RETRY_REACH"),
    FILE_ERROR ("FILE_ERROR"),
    NOTIFICATION_SENT ("NOTIFICATION_SENT"), 
    RESENT ("RESENT");

    final String name;

    StatusType(String s) {
        name = s;
    }

    @Override
    public String toString() {
        return this.name;
    }
}

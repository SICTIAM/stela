package fr.sictiam.stela.acteservice.model;

public enum StatusType {
    CREATED,
    SENT_INITIATED,
    ANTIVIRUS_OK,
    ANTIVIRUS_KO,
    SENT,
    NOT_SENT,
    ACK_RECEIVED,
    NACK_RECEIVED,
    FILE_ERROR
}

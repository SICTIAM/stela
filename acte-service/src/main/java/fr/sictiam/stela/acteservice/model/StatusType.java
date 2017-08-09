package fr.sictiam.stela.acteservice.model;

public enum StatusType {
    CREATED,
    ARCHIVE_CREATED,
    ANTIVIRUS_OK,
    ANTIVIRUS_KO,
    SENT,
    NOT_SENT,
    ACK_RECEIVED,
    NACK_RECEIVED,
    TO_CANCEL, FILE_ERROR
}

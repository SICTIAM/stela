package fr.sictiam.stela.pesservice.model;

public enum StatusType {
    CREATED("CREATED"),
    PENDING_SIGNATURE("PENDING_SIGNATURE"),
    SIGNATURE_INVALID("SIGNATURE_INVALID"),
    SIGNATURE_MISSING("SIGNATURE_MISSING"),
    CLASSEUR_WITHDRAWN("CLASSEUR_WITHDRAWN"),
    PENDING_SEND("PENDING_SEND"),
    SENT("SENT"),
    NOT_SENT("NOT_SENT"),
    ACK_RECEIVED("ACK_RECEIVED"),
    NACK_RECEIVED("NACK_RECEIVED"),
    PES_RETOUR_RECEIVED("PES_RETOUR_RECEIVED"),
    MAX_RETRY_REACH("MAX_RETRY_REACH"),
    FILE_ERROR("FILE_ERROR"),
    NOTIFICATION_SENT("NOTIFICATION_SENT"),
    RESENT("RESENT"),
    MANUAL_RESENT("MANUAL_RESENT"),
    SIGNATURE_SENDING_ERROR("SIGNATURE_SENDING_ERROR"),
    SENT_TO_SAE("SENT_TO_SAE"),
    ACCEPTED_BY_SAE("ACCEPTED_BY_SAE");

    final String name;

    StatusType(String s) {
        name = s;
    }

    @Override
    public String toString() {
        return this.name;
    }
}

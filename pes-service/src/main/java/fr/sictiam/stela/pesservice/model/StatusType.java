package fr.sictiam.stela.pesservice.model;

public enum StatusType {
    CREATED("CREATED", false),
    CREATION_IN_PROGRESS("CREATION_IN_PROGRESS", false),
    RECREATED("RECREATED", false),
    PENDING_SIGNATURE("PENDING_SIGNATURE", false),
    SIGNATURE_INVALID("SIGNATURE_INVALID", true),
    SIGNATURE_MISSING("SIGNATURE_MISSING", true),
    CLASSEUR_WITHDRAWN("CLASSEUR_WITHDRAWN", false),
    PENDING_SEND("PENDING_SEND", false),
    SENT("SENT", false),
    NOT_SENT("NOT_SENT", true),
    ACK_RECEIVED("ACK_RECEIVED", false),
    NACK_RECEIVED("NACK_RECEIVED", true),
    PES_RETOUR_RECEIVED("PES_RETOUR_RECEIVED", false),
    MAX_RETRY_REACH("MAX_RETRY_REACH", true),
    FILE_ERROR("FILE_ERROR", true),
    NOTIFICATION_SENT("NOTIFICATION_SENT", false),
    GROUP_NOTIFICATION_SENT("GROUP_NOTIFICATION_SENT", false),
    RESENT("RESENT", false),
    MANUAL_RESENT("MANUAL_RESENT", false),
    SIGNATURE_SENDING_ERROR("SIGNATURE_SENDING_ERROR", true),
    SENT_TO_SAE("SENT_TO_SAE", false),
    ACCEPTED_BY_SAE("ACCEPTED_BY_SAE", false);

    final String name;
    final boolean anomaly;

    StatusType(String name, boolean anomaly) {
        this.name = name;
        this.anomaly = anomaly;
    }

    public boolean isAnomaly() {
        return anomaly;
    }

    @Override
    public String toString() {
        return name;
    }
}

package fr.sictiam.stela.convocationservice.model;

public enum HistoryType {
    /* History for author actions */
    CREATED,
    SENT,
    ANNEXES_ADDED,
    RECIPIENTS_ADDED,
    QUESTIONS_ADDED,
    COMMENT_MODIFIED,
    MINUTES_ADDED,
    CANCELLED,
    GROUP_NOTIFICATION_SENT,
    NOTIFICATION_SENT,
    /* History for recipients actions */
    CONVOCATION_READ,
    CONVOCATION_RESPONSE,
    QUESTION_RESPONSE
}

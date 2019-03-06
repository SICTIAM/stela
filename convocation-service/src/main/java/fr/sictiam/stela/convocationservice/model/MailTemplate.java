package fr.sictiam.stela.convocationservice.model;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.lang.Nullable;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.Size;

@Entity
public class MailTemplate {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    @Size(max = 255)
    private String subject;

    @Size(max = 2048)
    private String body;

    @Nullable
    private String localAuthorityUuid;

    public MailTemplate() {
    }

    public MailTemplate(NotificationType notificationType, String subject, String body,
            @Nullable String localAuthorityUuid) {
        this.notificationType = notificationType;
        this.subject = subject;
        this.body = body;
        this.localAuthorityUuid = localAuthorityUuid;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Nullable public String getLocalAuthorityUuid() {
        return localAuthorityUuid;
    }

    public void setLocalAuthorityUuid(@Nullable String localAuthorityUuid) {
        this.localAuthorityUuid = localAuthorityUuid;
    }
}

package fr.sictiam.stela.pesservice.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.sictiam.stela.pesservice.config.LocalDateTimeDeserializer;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class PesHistory implements Comparable<PesHistory> {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
    private String pesUuid;
    @Enumerated(EnumType.STRING)
    private StatusType status;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime date;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Attachment attachment;
    // Error messages can be quite lengthy
    @Column(length = 1024)
    private String message;
    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonBinaryType")
    List<PesHistoryError> errors;

    public PesHistory() {
    }

    public PesHistory(String pesUuid, StatusType status) {
        this.pesUuid = pesUuid;
        this.status = status;
        date = LocalDateTime.now();
    }

    public PesHistory(String pesUuid, StatusType status, List<PesHistoryError> errors) {
        this.pesUuid = pesUuid;
        this.status = status;
        date = LocalDateTime.now();
        this.errors = errors;
    }

    public PesHistory(String pesUuid, StatusType status, String message) {
        this.pesUuid = pesUuid;
        this.status = status;
        date = LocalDateTime.now();
        this.message = message;
    }

    public PesHistory(String pesUuid, StatusType status, LocalDateTime date) {
        this.pesUuid = pesUuid;
        this.status = status;
        this.date = date;
    }

    public PesHistory(String pesUuid, StatusType status, LocalDateTime date, List<PesHistoryError> errors) {
        this.pesUuid = pesUuid;
        this.status = status;
        this.date = date;
        this.errors = errors;
    }

    public PesHistory(String pesUuid, StatusType status, LocalDateTime date, String message) {
        this.pesUuid = pesUuid;
        this.status = status;
        this.date = date;
        this.message = message;
    }

    public PesHistory(String pesUuid, StatusType status, LocalDateTime date, Attachment attachment) {
        this.pesUuid = pesUuid;
        this.status = status;
        this.date = date;
        this.attachment = attachment;
    }

    public PesHistory(String pesUuid, StatusType status, LocalDateTime date, Attachment attachment,
            List<PesHistoryError> errors) {
        this.pesUuid = pesUuid;
        this.status = status;
        this.date = date;
        this.attachment = attachment;
        this.errors = errors;
    }

    public PesHistory(String pesUuid, StatusType status, LocalDateTime date, Attachment attachment,
            String message) {

        this.pesUuid = pesUuid;
        this.status = status;
        this.date = date;
        this.attachment = attachment;
        this.message = message;
    }

    public String getUuid() {
        return uuid;
    }

    public String getPesUuid() {
        return pesUuid;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public StatusType getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<PesHistoryError> getErrors() {
        return errors != null ? errors : new ArrayList<>();
    }

    public void setErrors(List<PesHistoryError> errors) {
        this.errors = errors;
    }

    public void addError(PesHistoryError error) {
        if (errors == null)
            errors = new ArrayList<>();
        errors.add(error);
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }

    @Override
    public int compareTo(PesHistory acteHistory) {
        return date.compareTo(acteHistory.getDate());
    }

    @Override
    public String toString() {
        return "ActeHistory{" + "uuid='" + uuid + '\'' + ", pesUuid='" + pesUuid + '\'' + ", status=" + status
                + ", date=" + date + ", fileName='" + attachment.getFilename() + '\'' + ", message='" + message + "\'}";
    }
}

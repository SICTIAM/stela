package fr.sictiam.stela.acteservice.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.sictiam.stela.acteservice.config.LocalDateTimeDeserializer;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class PendingMessage {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;

    private String acteUuid;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime date;

    @Column(length = 1024)
    private String message;
    private byte[] file;

    private String fileName;

    public PendingMessage() {
    }

    public PendingMessage(ActeHistory acteHistory) {
        this.acteUuid = acteHistory.getActeUuid();
        this.date = acteHistory.getDate();
        this.file = acteHistory.getFile();
        this.fileName = acteHistory.getFileName();
    }

    public String getActeUuid() {
        return acteUuid;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getMessage() {
        return message;
    }

    public byte[] getFile() {
        return file;
    }

    public String getFileName() {
        return fileName;
    }

}

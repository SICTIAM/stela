package fr.sictiam.stela.pesservice.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import fr.sictiam.stela.pesservice.config.LocalDateTimeDeserializer;

@Entity
public class PendingMessage {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;

    private String pesUuid;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime date;

    @Column(length = 1024)
    private String message;
    private byte[] file;

    private String fileName;

    public PendingMessage() {
    }

    public PendingMessage(PesHistory pesHistory) {
        this.pesUuid = pesHistory.getPesUuid();
        this.date = pesHistory.getDate();
        this.file = pesHistory.getFile();
        this.fileName = pesHistory.getFileName();
    }

    public String getPesUuid() {
        return pesUuid;
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

package fr.sictiam.stela.acteservice.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.sictiam.stela.acteservice.config.LocalDateTimeDeserializer;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ActeHistory implements Comparable<ActeHistory> {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
    private String acteUuid;
    @Enumerated(EnumType.STRING)
    private StatusType status;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime date;
    // Error messages can be quite lengthy
    @Column(length = 1024)
    private String message;
    private byte[] file;
    private String fileName;

    public ActeHistory() {
    }

    public ActeHistory(String acteUuid, StatusType status) {
        this.acteUuid = acteUuid;
        this.status = status;
        this.date = LocalDateTime.now();
    }

    public ActeHistory(String acteUuid, StatusType status, String message) {
        this.acteUuid = acteUuid;
        this.status = status;
        this.date = LocalDateTime.now();
        this.message = message;
    }

    public ActeHistory(String acteUuid, StatusType status, LocalDateTime date, byte[] file, String fileName) {
        this.acteUuid = acteUuid;
        this.status = status;
        this.date = date;
        this.file = file;
        this.fileName = fileName;
    }

    public String getUuid() {
        return uuid;
    }

    public String getActeUuid() {
        return acteUuid;
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

    public byte[] getFile() {
        return file;
    }

    public String getFileName() {
        return fileName;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public int compareTo(ActeHistory acteHistory) {
        return this.date.compareTo(acteHistory.getDate());
    }

    @Override
    public String toString() {
        return "ActeHistory{" +
                "uuid='" + uuid + '\'' +
                ", acteUuid='" + acteUuid + '\'' +
                ", status=" + status +
                ", date=" + date +
                ", message='" + message + '\'' +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}

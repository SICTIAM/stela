package fr.sictiam.stela.pesservice.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.sictiam.stela.pesservice.config.LocalDateTimeDeserializer;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

import java.time.LocalDateTime;

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
    // Error messages can be quite lengthy
    @Column(length = 1024)
    private String message;
    private byte[] file;
    private String fileName;

    public PesHistory() {
    }

    public PesHistory(String pesUuid, StatusType status) {
        this.pesUuid = pesUuid;
        this.status = status;
        this.date = LocalDateTime.now();
    }

    public PesHistory(String pesUuid, StatusType status, LocalDateTime date, String message) {
        this.pesUuid = pesUuid;
        this.status = status;
        this.date = date;
        this.message = message;
    }

    public PesHistory(String pesUuid, StatusType status, LocalDateTime date, byte[] file, String fileName) {
        this.pesUuid = pesUuid;
        this.status = status;
        this.date = date;
        this.file = file;
        this.fileName = fileName;
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
    public int compareTo(PesHistory acteHistory) {
        return this.date.compareTo(acteHistory.getDate());
    }

    @Override
    public String toString() {
        return "ActeHistory{" + "uuid='" + uuid + '\'' + ", pesUuid='" + pesUuid + '\'' + ", status=" + status
                + ", date=" + date + ", message='" + message + '\'' + ", fileName='" + fileName + '\'' + '}';
    }
}

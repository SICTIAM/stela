package fr.sictiam.stela.convocationservice.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.sictiam.stela.convocationservice.config.LocalDateTimeDeserializer;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class ConvocationHistory implements Comparable<ConvocationHistory> {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
    private String convocationUuid;
    @Enumerated(EnumType.STRING)
    private StatusType status;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime date;
    // Error messages can be quite lengthy
    @Column(length = 1024)
    private String message;
    private byte[] file;
    private String fileName;

    public ConvocationHistory() {
    }

    public ConvocationHistory(String pesUuid, StatusType status) {
        this.convocationUuid = pesUuid;
        this.status = status;
        this.date = LocalDateTime.now();
    }

    public ConvocationHistory(String pesUuid, StatusType status, LocalDateTime date, String message) {
        this.convocationUuid = pesUuid;
        this.status = status;
        this.date = date;
        this.message = message;
    }

    public ConvocationHistory(String pesUuid, StatusType status, LocalDateTime date, byte[] file, String fileName) {
        this.convocationUuid = pesUuid;
        this.status = status;
        this.date = date;
        this.file = file;
        this.fileName = fileName;
    }

    public ConvocationHistory(String pesUuid, StatusType status, LocalDateTime date, byte[] file, String fileName,
            String message) {
        this.convocationUuid = pesUuid;
        this.status = status;
        this.date = date;
        this.file = file;
        this.fileName = fileName;
        this.message = message;
    }

    public String getUuid() {
        return uuid;
    }

    public String getConvocationUuid() {
        return convocationUuid;
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
    public String toString() {
        return "ActeHistory{" + "uuid='" + uuid + '\'' + ", convocationUuid='" + convocationUuid + '\'' + ", status="
                + status + ", date=" + date + ", message='" + message + '\'' + ", fileName='" + fileName + '\'' + '}';
    }

    @Override
    public int compareTo(ConvocationHistory o) {
        // TODO Auto-generated method stub
        return 0;
    }
}

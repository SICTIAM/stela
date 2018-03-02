package fr.sictiam.stela.convocationservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.sictiam.stela.convocationservice.config.LocalDateTimeDeserializer;
import fr.sictiam.stela.convocationservice.model.ui.Views;
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
    @JsonView(Views.ConvocationViewPublic.class)
    private String uuid;
    @JsonView(Views.ConvocationViewPublic.class)
    private String convocationUuid;
    @Enumerated(EnumType.STRING)
    @JsonView(Views.ConvocationViewPublic.class)
    private StatusType status;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonView(Views.ConvocationViewPublic.class)
    private LocalDateTime date;
    // Error messages can be quite lengthy
    @Column(length = 1024)
    @JsonView(Views.ConvocationViewPublic.class)
    private String message;
    @JsonIgnore
    private byte[] file;
    @JsonView(Views.ConvocationViewPublic.class)
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

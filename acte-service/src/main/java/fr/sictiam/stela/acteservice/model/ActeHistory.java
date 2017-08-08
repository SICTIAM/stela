package fr.sictiam.stela.acteservice.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class ActeHistory {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
    private String acteUuid;
    private StatusType status;
    private LocalDateTime date;
    private String message;

    public ActeHistory() {
    }

    public ActeHistory(String acteUuid, StatusType status, LocalDateTime date, String message) {
        this.acteUuid = acteUuid;
        this.status = status;
        this.date = date;
        this.message = message;
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

    @Override
    public String toString() {
        return "ActeHistory{" +
                "uuid='" + uuid + '\'' +
                ", acteUuid='" + acteUuid + '\'' +
                ", status=" + status +
                ", date=" + date +
                ", message='" + message + '\'' +
                '}';
    }
}

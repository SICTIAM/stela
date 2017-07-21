package fr.sictiam.stela.acteservice.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class ActeHistory {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
    private String acteUuid;
    private StatusType status;
    @JsonFormat(pattern="dd/MM/yyyy - HH:mm")
    private Date date;

    public ActeHistory() {
    }

    public ActeHistory(String acteUuid, StatusType status, Date date) {
        this.acteUuid = acteUuid;
        this.status = status;
        this.date = date;
    }

    public String getActeUuid() {
        return acteUuid;
    }

    public void setActeUuid(String acteUuid) {
        this.acteUuid = acteUuid;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public StatusType getStatus() {
        return status;
    }

    public void setStatus(StatusType status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "PesHistory{" +
                "uuid='" + uuid + '\'' +
                ", acteUuid='" + acteUuid + '\'' +
                ", status=" + status +
                ", date=" + date +
                '}';
    }
}

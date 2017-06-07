package fr.sictiam.stela.pes.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class PesHistory {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
    private String pesUuid;
    private StatusType status;
    private String origin;
    private Date date;
    private String content;

    public PesHistory() {
    }

    public PesHistory(String pesUuid, StatusType status, String origin, Date date) {
        this.pesUuid = pesUuid;
        this.status = status;
        this.origin = origin;
        this.date = date;
    }

    public String getPesUuid() {
        return pesUuid;
    }

    public void setpPesUuid(String pesUuid) {
        this.pesUuid = pesUuid;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public StatusType getStatus() {
        return status;
    }

    public void setStatus(StatusType status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "PesHistory{" +
                "uuid='" + uuid + '\'' +
                ", pesUuid='" + pesUuid + '\'' +
                ", status=" + status +
                ", origin='" + origin + '\'' +
                ", date=" + date +
                ", content='" + content + '\'' +
                '}';
    }
}

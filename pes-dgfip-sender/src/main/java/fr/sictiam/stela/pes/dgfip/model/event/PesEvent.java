package fr.sictiam.stela.pes.dgfip.model.event;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
public class PesEvent {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
    private String pesId;
    private EventType eventType;
    private Date eventDate;

    public PesEvent() {
    }

    public PesEvent(String pesId, EventType eventType) {
        this.pesId = pesId;
        this.eventType = eventType;
        this.eventDate = new Date();
    }

    public String getPesId() {
        return pesId;
    }

    public void setPesId(String pesId) {
        this.pesId = pesId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }
}

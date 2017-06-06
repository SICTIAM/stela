package fr.sictiam.stela.pes.model.event;

import java.util.Date;

public class PesSentEvent {

    private String pesUuid;
    private String origin;
    private Date eventDate;

    public PesSentEvent() {
    }

    public PesSentEvent(String pesUuid, String origin, Date eventDate) {
        this.pesUuid = pesUuid;
        this.origin = origin;
        this.eventDate = eventDate;
    }

    public String getPesUuid() {
        return pesUuid;
    }

    public void setPesUuid(String pesUuid) {
        this.pesUuid = pesUuid;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }
}

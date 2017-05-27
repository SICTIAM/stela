package fr.sictiam.stela.pescommand.event;

import java.util.Date;

public class PesSentEvent {

    private String pesId;
    private Date sentDate;

    public PesSentEvent() {
    }

    public PesSentEvent(String pesId, Date sentDate) {
        this.pesId = pesId;
        this.sentDate = sentDate;
    }

    public String getPesId() {
        return pesId;
    }
    public Date getSentDate() {
        return sentDate;
    }
}

package fr.sictiam.stela.pes.dgfip.model;

import java.util.Date;

public class PesSend {

    private String pesId;
    private Date sentDate;

    public PesSend() {
    }

    public PesSend(String pesId, Date sentDate) {
        this.pesId = pesId;
        this.sentDate = sentDate;
    }

    public String getPesId() {
        return pesId;
    }
    public Date getSentDate() {
        return sentDate;
    }

    @Override
    public String toString() {
        return "{\"pesId\":\"" + pesId + '\"' + "," +
                "\"sentDate\":\"" + sentDate + '\"'  +
                '}';
    }
}

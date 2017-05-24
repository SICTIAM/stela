package fr.sictiam.stela.pescommand.event;

import java.util.UUID;

public class PesSendedEvent {

    private String id;
    private String pesId;
    private String dateSend;

    public PesSendedEvent() {

    }

    public PesSendedEvent(String id, String pesId, String dateSend) {

        this.id = id;
        this.pesId = pesId;
        this.dateSend = dateSend;
    }

    public String getId () {
        return id;
    }
    public String getPesId() {
        return pesId;
    }
    public String getDateSend() {
        return dateSend;
    }

}

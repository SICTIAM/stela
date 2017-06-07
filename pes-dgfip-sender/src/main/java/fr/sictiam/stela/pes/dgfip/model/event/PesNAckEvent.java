package fr.sictiam.stela.pes.dgfip.model.event;

import java.util.Date;

public class PesNAckEvent {

    private String pesUuid;
    private String origin;
    private Date eventDate;

    private String fileContent;

    public PesNAckEvent() {
    }

    public PesNAckEvent(String pesUuid, String origin, Date eventDate, String fileContent) {
        this.pesUuid = pesUuid;
        this.origin = origin;
        this.eventDate = eventDate;
        this.fileContent = fileContent;
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

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }
}

package fr.sictiam.stela.pes.model.event;

import fr.sictiam.stela.pes.model.Pes;

import java.util.Date;

public class PesCreatedEvent {

    private String pesUuid;
    private String origin;
    private Date eventDate;

    private String title;
    private String file;
    private String comment;

    public PesCreatedEvent() {
    }

    public PesCreatedEvent(Pes pes, String origin, Date eventDate) {
        this.pesUuid = pes.getUuid();
        this.origin = origin;
        this.eventDate = eventDate;

        this.title = pes.getTitle();
        this.file = pes.getFile();
        this.comment = pes.getComment();
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}

package fr.sictiam.stela.pes.model.event;

import fr.sictiam.stela.pes.model.Pes;

import java.util.Date;

public class PesCreatedEvent {

    private String pesUuid;
    private String origin;
    private Date eventDate;

    private String pesId;
    private String title;
    private String fileContent;
    private String fileName;
    private String comment;

    public PesCreatedEvent() {
    }

    public PesCreatedEvent(Pes pes, String origin, Date eventDate) {
        this.pesUuid = pes.getUuid();
        this.origin = origin;
        this.eventDate = eventDate;

        this.pesId = pes.getPesId();
        this.title = pes.getTitle();
        this.fileContent = pes.getFileContent();
        this.fileName = pes.getFileName();
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

    public String getPesId() {
        return pesId;
    }

    public void setPesId(String pesId) {
        this.pesId = pesId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}

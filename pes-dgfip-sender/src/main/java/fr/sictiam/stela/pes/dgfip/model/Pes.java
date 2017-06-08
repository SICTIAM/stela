package fr.sictiam.stela.pes.dgfip.model;

import java.util.Date;

public class Pes {

    private String uuid;
    private String title;
    private String file;
    private String comment;
    private Date creationDate;
    private StatusType status;

    public Pes() {
    }

    public Pes(String title, String file, String comment) {

        this.title = title;
        this.comment = comment;
        this.file = file;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public StatusType getStatus() {
        return status;
    }

    public void setStatus(StatusType status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Pes{" +
                "uuid='" + uuid + '\'' +
                ", title='" + title + '\'' +
                ", file='" + file + '\'' +
                ", comment='" + comment + '\'' +
                ", creationDate=" + creationDate +
                ", status=" + status +
                '}';
    }
}

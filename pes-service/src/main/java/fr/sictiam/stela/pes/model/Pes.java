package fr.sictiam.stela.pes.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Pes {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
    private String title;
    private String file;
    private String comment;

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

    @Override
    public String toString() {
        return "Pes{" +
                "uuid='" + uuid + '\'' +
                ", title='" + title + '\'' +
                ", file='" + file + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }
}

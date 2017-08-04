package fr.sictiam.stela.acteservice.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Attachment {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;

    private byte[] file;
    private String filename;

    public Attachment() {
    }

    public Attachment(byte[] file, String filename) {
        this.file = file;
        this.filename = filename;
    }

    public byte[] getFile() {
        return file;
    }

    public String getFilename() {
        return filename;
    }
}

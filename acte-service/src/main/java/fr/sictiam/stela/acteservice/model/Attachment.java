package fr.sictiam.stela.acteservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore
    private byte[] file;
    private String filename;
    private long size;
    private String attachmentTypeCode;

    public Attachment() {
    }

    public Attachment(byte[] file, String filename, long size) {
        this.file = file;
        this.filename = filename;
        this.size = size;
    }

    public String getUuid() {
        return uuid;
    }

    public byte[] getFile() {
        return file;
    }

    public String getFilename() {
        return filename;
    }

    public long getSize() {
        return size;
    }

    public String getAttachmentTypeCode() {
        return attachmentTypeCode;
    }

    public void setAttachmentTypeCode(String attachmentTypeCode) {
        this.attachmentTypeCode = attachmentTypeCode;
    }
}

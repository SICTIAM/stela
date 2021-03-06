package fr.sictiam.stela.acteservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import fr.sictiam.stela.acteservice.model.ui.Views;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Attachment {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JsonView(Views.AttachmentFullView.class)
    private String uuid;

    @JsonIgnore
    private byte[] file;
    @JsonView(Views.AttachmentFullView.class)
    private String filename;
    @JsonView(Views.AttachmentFullView.class)
    private long size;
    @JsonView(Views.AttachmentFullView.class)
    private String attachmentTypeCode;

    public Attachment() {
    }

    public Attachment(byte[] file, String filename, long size) {
        this.file = file;
        this.filename = filename;
        this.size = size;
    }

    public Attachment(byte[] file, String filename, long size, String attachmentTypeCode) {
        this.file = file;
        this.filename = filename;
        this.size = size;
        this.attachmentTypeCode = attachmentTypeCode;
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

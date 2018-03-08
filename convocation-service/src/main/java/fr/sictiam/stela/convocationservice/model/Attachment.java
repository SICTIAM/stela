package fr.sictiam.stela.convocationservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.sictiam.stela.convocationservice.config.LocalDateTimeDeserializer;
import fr.sictiam.stela.convocationservice.model.ui.Views;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class Attachment {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JsonView(Views.ConvocationViewPublic.class)
    private String uuid;

    @JsonIgnore
    private byte[] file;
    @JsonView(Views.ConvocationViewPublic.class)
    private String filename;
    @JsonView(Views.ConvocationViewPublic.class)
    private long size;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonView(Views.ConvocationViewPublic.class)
    private LocalDateTime date;

    public Attachment() {
    }

    public Attachment(byte[] file, String filename, long size) {
        this.file = file;
        this.filename = filename;
        this.size = size;
        this.date = LocalDateTime.now();
    }

    public String getUuid() {
        return uuid;
    }

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }

    public String getFilename() {
        return filename;
    }

    public long getSize() {
        return size;
    }

    public LocalDateTime getDate() {
        return date;
    }
}
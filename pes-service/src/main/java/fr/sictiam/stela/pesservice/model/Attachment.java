package fr.sictiam.stela.pesservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.sictiam.stela.pesservice.config.LocalDateTimeDeserializer;
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
    private String uuid;

    @JsonIgnore
    private byte[] file;
    private String filename;
    private long size;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
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

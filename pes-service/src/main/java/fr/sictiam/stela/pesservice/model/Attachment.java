package fr.sictiam.stela.pesservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.sictiam.stela.pesservice.config.LocalDateTimeDeserializer;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Attachment {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;

    private String filename;
    private long size;

    private String storageKey;

    @JsonIgnore
    transient private byte[] content;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime date;

    public Attachment() {
        this(null, null, 0, LocalDateTime.now());
    }

    public Attachment(String filename, byte[] content, long size, LocalDateTime date) {
        this.filename = filename;
        this.content = content;
        this.size = size;
        this.date = date;
        storageKey = "pes/" + UUID.randomUUID().toString();
    }

    public String getUuid() {
        return uuid;
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

    public String getStorageKey() {
        return storageKey;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void updateContent(byte[] content) {
        setContent(content);
        size = content.length;
    }

    public interface Light {
        String getUuid();

        String getFilename();

        long getSize();

        LocalDateTime getDate();
    }

}

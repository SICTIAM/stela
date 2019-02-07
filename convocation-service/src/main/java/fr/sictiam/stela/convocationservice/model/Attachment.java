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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Attachment {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JsonView(Views.ConvocationInternal.class)
    private String uuid;

    @JsonView(Views.ConvocationInternal.class)
    private String filename;

    private long size;

    private String storageKey;

    @JsonIgnore
    private byte[] content;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime date;

    public Attachment() {
        this(null, null, 0, LocalDateTime.now());
    }

    public Attachment(String filename, byte[] content) {
        this(filename, content, content != null ? content.length : 0, LocalDateTime.now());
    }

    public Attachment(String filename, byte[] content, long size, LocalDateTime date) {
        if (filename != null) {
            Path path = Paths.get(filename);
            this.filename = path.getFileName().toString();
        }
        this.content = content;
        this.size = size;
        this.date = date;
        storageKey = "convocation/" + UUID.randomUUID().toString();
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

    public void setFilename(String filename) {
        this.filename = filename;
    }
}

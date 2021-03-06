package fr.sictiam.stela.convocationservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import fr.sictiam.stela.convocationservice.model.ui.Views;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
public class Attachment {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JsonView(Views.Attachment.class)
    private String uuid;

    @JsonView(Views.Attachment.class)
    private String filename;

    @JsonIgnore
    private long size;

    @JsonIgnore
    private String storageKey;

    @JsonIgnore
    private byte[] content;

    @JsonIgnore
    private LocalDateTime date;

    @JsonView(Views.Attachment.class)
    private boolean additional = false;

    @ManyToMany
    @JoinTable(name = "attachment_tag",
            joinColumns = @JoinColumn(name = "attachment_uuid"),
            inverseJoinColumns = @JoinColumn(name = "tag_uuid"))
    @JsonView(Views.Attachment.class)
    private Set<Tag> tags;

    public Attachment() {
        this(null, null, 0, LocalDateTime.now(), false);
    }

    public Attachment(String filename, byte[] content) {
        this(filename, content, content != null ? content.length : 0, LocalDateTime.now(), false);
    }

    public Attachment(String filename, byte[] content, boolean additional) {
        this(filename, content, content != null ? content.length : 0, LocalDateTime.now(), additional);
    }

    public Attachment(String filename, byte[] content, long size, LocalDateTime date) {
        this(filename, content, content != null ? content.length : 0, date, false);
    }

    public Attachment(String filename, byte[] content, long size, LocalDateTime date, boolean additional) {
        if (filename != null) {
            Path path = Paths.get(filename);
            this.filename = path.getFileName().toString();
        }
        this.content = content;
        this.size = size;
        this.date = date;
        storageKey = "convocation/" + UUID.randomUUID().toString();
        this.additional = additional;
        tags = new HashSet<>();
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

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public boolean isAdditional() {
        return additional;
    }

    public void setAdditional(boolean additional) {
        this.additional = additional;
    }

    @Override public String toString() {
        return "{" +
                "\"uuid\": \"" + uuid + "\"" +
                ", \"filename\": \"'" + filename + "\"" +
                ", \"size\": \"" + size +
                ", \"storageKey\": \"'" + storageKey + "\"" +
                '}';
    }
}

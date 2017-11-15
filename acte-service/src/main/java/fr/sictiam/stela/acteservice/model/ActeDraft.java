package fr.sictiam.stela.acteservice.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ActeDraft {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
    private LocalDateTime lastModified;

    public ActeDraft() {
    }

    public ActeDraft(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public String getUuid() {
        return uuid;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }
}

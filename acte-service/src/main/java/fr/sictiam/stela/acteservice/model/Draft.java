package fr.sictiam.stela.acteservice.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Draft {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
    private LocalDateTime lastModified;
    private ActeMode mode;

    public Draft() {
    }

    public Draft(LocalDateTime lastModified, ActeMode mode) {
        this.lastModified = lastModified;
        this.mode = mode;
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

    public ActeMode getMode() {
        return mode;
    }
}

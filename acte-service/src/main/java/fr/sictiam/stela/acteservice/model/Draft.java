package fr.sictiam.stela.acteservice.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.sictiam.stela.acteservice.config.LocalDateDeserializer;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Draft {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
    private LocalDateTime lastModified;
    private ActeMode mode;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate decision;
    private ActeNature nature;
    private String groupUuid;

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

    public LocalDate getDecision() {
        return decision;
    }

    public void setDecision(LocalDate decision) {
        this.decision = decision;
    }

    public ActeNature getNature() {
        return nature;
    }

    public void setNature(ActeNature nature) {
        this.nature = nature;
    }

    public String getGroupUuid() {
        return groupUuid;
    }

    public void setGroupUuid(String groupUuid) {
        this.groupUuid = groupUuid;
    }
}

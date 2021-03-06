package fr.sictiam.stela.acteservice.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fr.sictiam.stela.acteservice.config.LocalDateDeserializer;
import fr.sictiam.stela.acteservice.config.LocalDateSerializer;
import fr.sictiam.stela.acteservice.config.LocalDateTimeSerializer;
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
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime lastModified;
    private ActeMode mode;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate decision;
    private ActeNature nature;
    private String groupUuid;
    private String localAuthorityUuid;

    public Draft() {
    }

    public Draft(LocalDateTime lastModified, ActeMode mode, String localAuthorityUuid) {
        this.lastModified = lastModified;
        this.mode = mode;
        this.localAuthorityUuid = localAuthorityUuid;
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

    public String getLocalAuthorityUuid() {
        return localAuthorityUuid;
    }

    public void setLocalAuthorityUuid(String localAuthorityUuid) {
        this.localAuthorityUuid = localAuthorityUuid;
    }
}

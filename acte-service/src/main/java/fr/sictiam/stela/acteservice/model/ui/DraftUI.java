package fr.sictiam.stela.acteservice.model.ui;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fr.sictiam.stela.acteservice.config.LocalDateSerializer;
import fr.sictiam.stela.acteservice.config.LocalDateTimeSerializer;
import fr.sictiam.stela.acteservice.model.ActeMode;
import fr.sictiam.stela.acteservice.model.ActeNature;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class DraftUI {

    private String uuid;
    private List<ActeDraftUI> actes;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime lastModified;
    private ActeMode mode;
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate decision;
    private ActeNature nature;
    private String groupUuid;

    public DraftUI() {
    }

    public DraftUI(String uuid, List<ActeDraftUI> actes, LocalDateTime lastModified, ActeMode mode, LocalDate decision,
            ActeNature nature, String groupUuid) {
        this.uuid = uuid;
        this.actes = actes;
        this.lastModified = lastModified;
        this.mode = mode;
        this.decision = decision;
        this.nature = nature;
        this.groupUuid = groupUuid;
    }

    public String getUuid() {
        return uuid;
    }

    public List<ActeDraftUI> getActes() {
        return actes;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public ActeMode getMode() {
        return mode;
    }

    public LocalDate getDecision() {
        return decision;
    }

    public ActeNature getNature() {
        return nature;
    }

    public String getGroupUuid() {
        return groupUuid;
    }
}

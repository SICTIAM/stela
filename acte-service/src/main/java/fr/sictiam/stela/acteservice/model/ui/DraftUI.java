package fr.sictiam.stela.acteservice.model.ui;

import fr.sictiam.stela.acteservice.model.ActeMode;
import fr.sictiam.stela.acteservice.model.ActeNature;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class DraftUI {

    private String uuid;
    private List<ActeDraftUI> actes;
    private LocalDateTime lastModified;
    private ActeMode mode;
    private LocalDate decision;
    private ActeNature nature;

    public DraftUI() {
    }

    public DraftUI(String uuid, List<ActeDraftUI> actes, LocalDateTime lastModified, ActeMode mode, LocalDate decision, ActeNature nature) {
        this.uuid = uuid;
        this.actes = actes;
        this.lastModified = lastModified;
        this.mode = mode;
        this.decision = decision;
        this.nature = nature;
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
}

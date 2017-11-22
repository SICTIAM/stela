package fr.sictiam.stela.acteservice.model.ui;

import fr.sictiam.stela.acteservice.model.ActeMode;

import java.time.LocalDateTime;
import java.util.List;

public class DraftUI {

    private String uuid;
    private List<ActeDraftUI> actes;
    private LocalDateTime lastModified;
    private ActeMode mode;

    public DraftUI(String uuid, List<ActeDraftUI> actes, LocalDateTime lastModified, ActeMode mode) {
        this.uuid = uuid;
        this.actes = actes;
        this.lastModified = lastModified;
        this.mode = mode;
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
}

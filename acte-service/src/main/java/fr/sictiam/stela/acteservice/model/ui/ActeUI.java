package fr.sictiam.stela.acteservice.model.ui;

import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.model.ActeHistory;
import fr.sictiam.stela.acteservice.model.StampPosition;

public class ActeUI {

    private Acte acte;
    private boolean acteACK;
    private ActeHistory lastMetierHistory;
    private StampPosition stampPosition;

    public ActeUI(Acte acte, boolean acteACK, ActeHistory lastMetierHistory, StampPosition stampPosition) {
        this.acte = acte;
        this.acteACK = acteACK;
        this.lastMetierHistory = lastMetierHistory;
        this.stampPosition = stampPosition;
    }

    public Acte getActe() {
        return acte;
    }

    public boolean isActeACK() {
        return acteACK;
    }

    public ActeHistory getLastMetierHistory() {
        return lastMetierHistory;
    }

    public StampPosition getStampPosition() {
        return stampPosition;
    }
}

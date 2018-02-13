package fr.sictiam.stela.acteservice.model.ui;

import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.model.StampPosition;

public class ActeUI {

    private Acte acte;
    private boolean acteACK;
    private StampPosition stampPosition;

    public ActeUI(Acte acte, boolean acteACK, StampPosition stampPosition) {
        this.acte = acte;
        this.acteACK = acteACK;
        this.stampPosition = stampPosition;
    }

    public Acte getActe() {
        return acte;
    }

    public boolean isActeACK() {
        return acteACK;
    }

    public StampPosition getStampPosition() {
        return stampPosition;
    }
}

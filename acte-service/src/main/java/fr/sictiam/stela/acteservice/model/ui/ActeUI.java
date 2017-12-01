package fr.sictiam.stela.acteservice.model.ui;

import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.model.ActeHistory;
import fr.sictiam.stela.acteservice.model.StampPosition;

import java.util.List;

public class ActeUI {

    private Acte acte;
    private boolean isCancellable;
    private StampPosition stampPosition;

    public ActeUI(Acte acte, boolean isCancellable, StampPosition stampPosition) {
        this.acte = acte;
        this.isCancellable = isCancellable;
        this.stampPosition = stampPosition;
    }

    public Acte getActe() {
        return acte;
    }

    public boolean isCancellable() {
        return isCancellable;
    }

    public StampPosition getStampPosition() {
        return stampPosition;
    }
}

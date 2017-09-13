package fr.sictiam.stela.acteservice.model.ui;

import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.model.ActeHistory;

import java.util.List;

public class ActeUI {

    private Acte acte;
    private boolean isCancellable;

    public ActeUI(Acte acte, boolean isCancellable) {
        this.acte = acte;
        this.isCancellable = isCancellable;
    }

    public Acte getActe() {
        return acte;
    }

    public boolean isCancellable() {
        return isCancellable;
    }
}

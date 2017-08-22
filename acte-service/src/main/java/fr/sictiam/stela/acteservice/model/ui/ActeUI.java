package fr.sictiam.stela.acteservice.model.ui;

import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.model.ActeHistory;

import java.util.List;

public class ActeUI {

    private Acte acte;
    private List<ActeHistory> history;
    private boolean isCancellable;

    public ActeUI(Acte acte, List<ActeHistory> history, boolean isCancellable) {
        this.acte = acte;
        this.history = history;
        this.isCancellable = isCancellable;
    }

    public Acte getActe() {
        return acte;
    }

    public List<ActeHistory> getHistory() {
        return history;
    }

    public boolean isCancellable() {
        return isCancellable;
    }
}

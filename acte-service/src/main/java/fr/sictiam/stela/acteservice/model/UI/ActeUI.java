package fr.sictiam.stela.acteservice.model.UI;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.model.ActeHistory;

import java.util.List;

public class ActeUI {

    private Acte acte;
    private List<ActeHistory> history;
    private boolean isCancellable;


    public ActeUI() {
    }

    public ActeUI(Acte acte, List<ActeHistory> history, boolean isCancellable) {
        this.acte = acte;
        this.history = history;
        this.isCancellable = isCancellable;
    }


    public Acte getActe() {
        return acte;
    }

    public void setActe(Acte acte) {
        this.acte = acte;
    }

    public List<ActeHistory> getHistory() {
        return history;
    }

    public void setHistory(List<ActeHistory> history) {
        this.history = history;
    }

    public boolean isCancellable() {
        return isCancellable;
    }

    public void setCancellable(boolean cancellable) {
        isCancellable = cancellable;
    }
}

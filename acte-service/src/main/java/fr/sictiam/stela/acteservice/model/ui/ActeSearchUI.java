package fr.sictiam.stela.acteservice.model.ui;

import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.model.StatusType;

import java.time.LocalDate;

public class ActeSearchUI {

    private Acte acte;
    private LocalDate decisionFrom;
    private LocalDate decisionTo;
    private StatusType status;

    public ActeSearchUI() {
    }

    public Acte getActe() {
        return acte;
    }

    public void setActe(Acte acte) {
        this.acte = acte;
    }

    public LocalDate getDecisionFrom() {
        return decisionFrom;
    }

    public void setDecisionFrom(LocalDate decisionFrom) {
        this.decisionFrom = decisionFrom;
    }

    public LocalDate getDecisionTo() {
        return decisionTo;
    }

    public void setDecisionTo(LocalDate decisionTo) {
        this.decisionTo = decisionTo;
    }

    public StatusType getStatus() {
        return status;
    }

    public void setStatus(StatusType status) {
        this.status = status;
    }
}

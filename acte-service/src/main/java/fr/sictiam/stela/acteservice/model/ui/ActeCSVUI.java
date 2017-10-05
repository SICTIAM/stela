package fr.sictiam.stela.acteservice.model.ui;

import java.util.Arrays;
import java.util.List;

public class ActeCSVUI {

    private String number;
    private String objet;
    private String decision;
    private String nature;
    private String status;

    public ActeCSVUI() {
    }

    public ActeCSVUI(String number, String objet, String decision, String nature, String status) {
        this.number = number;
        this.objet = objet;
        this.decision = decision;
        this.nature = nature;
        this.status = status;
    }

    public String getNumber() {
        return number;
    }

    public String getObjet() {
        return objet;
    }

    public String getDecision() {
        return decision;
    }

    public String getNature() {
        return nature;
    }

    public String getStatus() {
        return status;
    }

    static public List<String> getFields() {
        return Arrays.asList("number", "objet", "decision", "nature", "status");
    }
}

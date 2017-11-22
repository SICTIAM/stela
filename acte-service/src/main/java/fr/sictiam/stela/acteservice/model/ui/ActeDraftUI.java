package fr.sictiam.stela.acteservice.model.ui;

import fr.sictiam.stela.acteservice.model.ActeNature;

public class ActeDraftUI {

    private String uuid;
    private String number;
    private String objet;
    private ActeNature nature;

    public ActeDraftUI(String uuid, String number, String objet, ActeNature nature) {
        this.uuid = uuid;
        this.number = number;
        this.objet = objet;
        this.nature = nature;
    }

    public String getUuid() {
        return uuid;
    }

    public String getNumber() {
        return number;
    }

    public String getObjet() {
        return objet;
    }

    public ActeNature getNature() {
        return nature;
    }
}
package fr.sictiam.stela.pesservice.model.ui;

import java.util.List;

public class SesileServiceOrganisation {

    private String id;

    private String nom;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public List<Integer> getType_classeur() {
        return type_classeur;
    }

    public void setType_classeur(List<Integer> type_classeur) {
        this.type_classeur = type_classeur;
    }

    private List<Integer> type_classeur;
}

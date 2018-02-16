package fr.sictiam.stela.pesservice.model.sesile;

import java.util.List;

public class ServiceOrganisation {

    private Integer id;

    private String nom;

    private List<Integer> type_classeur;

    private List<ClasseurType> types;

    public List<ClasseurType> getTypes() {
        return types;
    }

    public void setTypes(List<ClasseurType> types) {
        this.types = types;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

}

package fr.sictiam.stela.pesservice.model.sesile;

import java.util.List;

public class Classeur {
    // {"id":2892,"nom":"test","description":"test","creation":"2018-02-13T18:02:58+0100","validation":"2018-02-20T00:00:00+0100","type":2,"validant":[{"id":3,"nom":"Anne-Sophie,
    // Charlotte LEVEQUE"},{"id":38,"nom":"Fred Laussinot"},{"id":40,"nom":"BENOIT
    // COLINET2"}],"visibilite":3,"status":1,"documents":[],"actions":[]}
    private int id;

    private String nom;

    private String description;

    private String creation;

    private String validation;

    private int type;

    private ClasseurStatus status;

    private List<Document> documents;

    public ClasseurStatus getStatus() {
        return status;
    }

    public void setStatus(ClasseurStatus status) {
        this.status = status;
    }

    private int visibilite;

    private String email;

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getValidation() {
        return validation;
    }

    public void setValidation(String validation) {
        this.validation = validation;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCreation() {
        return creation;
    }

    public void setCreation(String creation) {
        this.creation = creation;
    }

    public int getVisibilite() {
        return visibilite;
    }

    public void setVisibilite(int visibilite) {
        this.visibilite = visibilite;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

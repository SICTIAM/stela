package fr.sictiam.stela.pesservice.model.asalae;

public class AsalaeInfo {

    private String id_d;
    private String type;
    private String titre;
    private String creation;
    private String modification;

    public AsalaeInfo() {
    }

    public String getId_d() {
        return id_d;
    }

    public void setId_d(String id_d) {
        this.id_d = id_d;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getCreation() {
        return creation;
    }

    public void setCreation(String creation) {
        this.creation = creation;
    }

    public String getModification() {
        return modification;
    }

    public void setModification(String modification) {
        this.modification = modification;
    }

    @Override
    public String toString() {
        return "AsalaeInfo{ " +
                "id_d='" + id_d + '\'' +
                ", type='" + type + '\'' +
                ", titre='" + titre + '\'' +
                ", creation=" + creation +
                ", modification=" + modification +
                " }";
    }
}

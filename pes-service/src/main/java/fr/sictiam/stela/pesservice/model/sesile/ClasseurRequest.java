package fr.sictiam.stela.pesservice.model.sesile;

public class ClasseurRequest {

    private String name;

    private String desc;

    private String validation;

    private int type;

    private int groupe;

    private int visibilite;

    private String email;

    public ClasseurRequest(String name, String desc, String validation, int type, int groupe, int visibilite,
            String email) {
        this.name = name;
        this.desc = desc;
        this.validation = validation;
        this.type = type;
        this.groupe = groupe;
        this.visibilite = visibilite;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
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

    public int getGroupe() {
        return groupe;
    }

    public void setGroupe(int groupe) {
        this.groupe = groupe;
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

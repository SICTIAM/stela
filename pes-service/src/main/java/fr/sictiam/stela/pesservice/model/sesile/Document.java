package fr.sictiam.stela.pesservice.model.sesile;

public class Document {
    // {"id":2894,"name":"28000-2017-P-RN-22-1516807373820","repourl":"5a831da0334e6.","type":"application\/xml","signed":false,"histories":[]}

    private int id;

    private String nom;

    private boolean signed;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public boolean isSigned() {
        return signed;
    }

    public void setSigned(boolean signed) {
        this.signed = signed;
    }
}

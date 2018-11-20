package fr.sictiam.stela.pesservice.model.sesile;


/**
 * Class used for the Sesile4 new deposit API
 */
public class ClasseurSirenRequest extends ClasseurRequest {

    private String siren;
    private String callback;

    public ClasseurSirenRequest(ClasseurRequest classeurRequest, String siren, String callback) {
        super(classeurRequest.getName(), classeurRequest.getDesc(), classeurRequest.getValidation(),
                classeurRequest.getType(), classeurRequest.getGroupe(), classeurRequest.getVisibilite(),
                classeurRequest.getEmail());
        this.siren = siren;
        this.callback = callback;
    }

    public String getSiren() {
        return siren;
    }

    public void setSiren(String siren) {
        this.siren = siren;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    @Override
    public String toString() {
        return "ClasseurRequest [name=" + getName() + ", desc=" + getDesc() + ", validation=" + getValidation()
                + ", type=" + getType() + ", groupe=" + getGroupe() + ", visibilite=" + getVisibilite()
                + ", email=" + getEmail() + ", siren=" + siren + ", callback=" + callback + "]";
    }
}

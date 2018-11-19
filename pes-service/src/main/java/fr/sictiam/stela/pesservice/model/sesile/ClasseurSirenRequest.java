package fr.sictiam.stela.pesservice.model.sesile;


/**
 * Class used for the Sesile4 new deposit API
 */
public class ClasseurSirenRequest extends ClasseurRequest {

    private String siren;
    private String returnUrl;

    public ClasseurSirenRequest(ClasseurRequest classeurRequest, String siren, String returnUrl) {
        super(classeurRequest.getName(), classeurRequest.getDesc(), classeurRequest.getValidation(),
                classeurRequest.getType(), classeurRequest.getGroupe(), classeurRequest.getVisibilite(),
                classeurRequest.getEmail());
        this.siren = siren;
        this.returnUrl = returnUrl;
    }

    public String getSiren() {
        return siren;
    }

    public void setSiren(String siren) {
        this.siren = siren;
    }

    @Override
    public String toString() {
        return "ClasseurRequest [name=" + getName() + ", desc=" + getDesc() + ", validation=" + getValidation()
                + ", type=" + getType() + ", groupe=" + getGroupe() + ", visibilite=" + getVisibilite()
                + ", email=" + getEmail() + ", siren=" + siren + ", returnUrl=" + returnUrl + "]";
    }
}

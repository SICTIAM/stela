package fr.sictiam.stela.acteservice.model.asalae;

public class AsalaeResultForm {

    private String result;
    private String formulaire_ok;
    private String message;
    private AsalaeDocument content;

    public AsalaeResultForm() {
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getFormulaire_ok() {
        return formulaire_ok;
    }

    public void setFormulaire_ok(String formulaire_ok) {
        this.formulaire_ok = formulaire_ok;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public AsalaeDocument getContent() {
        return content;
    }

    public void setContent(AsalaeDocument content) {
        this.content = content;
    }
}

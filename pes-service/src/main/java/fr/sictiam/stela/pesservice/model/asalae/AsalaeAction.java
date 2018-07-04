package fr.sictiam.stela.pesservice.model.asalae;

public class AsalaeAction {

    private String action;
    private String message;
    private String date;

    public AsalaeAction() {
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "AsalaeAction{ " +
                "action='" + action + '\'' +
                ", message='" + message + '\'' +
                ", date=" + date +
                " }";
    }
}

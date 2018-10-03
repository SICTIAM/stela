package fr.sictiam.stela.apigateway.model;

public class Notification {

    private String type;
    private boolean deactivatable;
    private boolean defaultValue;

    public Notification() {
    }

    public Notification(String type, boolean deactivatable, boolean defaultValue) {
        this.type = type;
        this.deactivatable = deactivatable;
        this.defaultValue = defaultValue;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isDeactivatable() {
        return deactivatable;
    }

    public void setDeactivatable(boolean deactivatable) {
        this.deactivatable = deactivatable;
    }

    public boolean isDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(boolean defaultValue) {
        this.defaultValue = defaultValue;
    }
}

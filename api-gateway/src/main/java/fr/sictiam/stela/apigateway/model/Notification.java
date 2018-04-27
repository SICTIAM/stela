package fr.sictiam.stela.apigateway.model;

public class Notification {

    private String statusType;
    private boolean deactivatable;
    private boolean defaultValue;

    public Notification() {
    }

    public Notification(String statusType, boolean deactivatable, boolean defaultValue) {
        this.statusType = statusType;
        this.deactivatable = deactivatable;
        this.defaultValue = defaultValue;
    }

    public String getStatusType() {
        return statusType;
    }

    public void setStatusType(String statusType) {
        this.statusType = statusType;
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

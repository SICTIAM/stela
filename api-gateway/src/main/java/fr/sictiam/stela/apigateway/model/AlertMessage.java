package fr.sictiam.stela.apigateway.model;

public class AlertMessage {

    private String alertMessage;
    private boolean alertMessageDisplayed;

    public AlertMessage() {
    }

    public String getAlertMessage() {
        return alertMessage;
    }

    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
    }

    public boolean isAlertMessageDisplayed() {
        return alertMessageDisplayed;
    }

    public void setAlertMessageDisplayed(boolean alertMessageDisplayed) {
        this.alertMessageDisplayed = alertMessageDisplayed;
    }
}
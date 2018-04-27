package fr.sictiam.stela.convocationservice.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Admin {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;

    private boolean alertMessageDisplayed;

    private String alertMessage;

    public Admin() {

    }

    public Admin(String uuid, boolean alertMessageDisplayed, String alertMessage) {
        this.uuid = uuid;
        this.alertMessageDisplayed = alertMessageDisplayed;
        this.alertMessage = alertMessage;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isAlertMessageDisplayed() {
        return alertMessageDisplayed;
    }

    public void setAlertMessageDisplayed(boolean alertMessageDisplayed) {
        this.alertMessageDisplayed = alertMessageDisplayed;
    }

    public String getAlertMessage() {
        return alertMessage;
    }

    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
    }
}

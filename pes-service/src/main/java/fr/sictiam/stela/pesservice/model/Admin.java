package fr.sictiam.stela.pesservice.model;

import com.fasterxml.jackson.annotation.JsonView;
import fr.sictiam.stela.pesservice.model.ui.Views;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
public class Admin {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;

    @NotNull
    private boolean heliosAvailable;
    @NotNull
    private LocalDateTime unavailabilityHeliosStartDate;
    @NotNull
    private LocalDateTime unavailabilityHeliosEndDate;
    @JsonView(Views.AdminAlertMessageView.class)
    private boolean alertMessageDisplayed;
    @JsonView(Views.AdminAlertMessageView.class)
    private String alertMessage;

    public Admin() {

    }

    public Admin(String uuid, @NotNull boolean heliosAvailable, @NotNull LocalDateTime unavailabilityHeliosStartDate,
            @NotNull LocalDateTime unavailabilityHeliosEndDate, boolean alertMessageDisplayed, String alertMessage) {
        this.uuid = uuid;
        this.heliosAvailable = heliosAvailable;
        this.unavailabilityHeliosStartDate = unavailabilityHeliosStartDate;
        this.unavailabilityHeliosEndDate = unavailabilityHeliosEndDate;
        this.alertMessageDisplayed = alertMessageDisplayed;
        this.alertMessage = alertMessage;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isHeliosAvailable() {
        return heliosAvailable;
    }

    public void setHeliosAvailable(boolean heliosAvailable) {
        this.heliosAvailable = heliosAvailable;
    }

    public LocalDateTime getUnavailabilityHeliosStartDate() {
        return unavailabilityHeliosStartDate;
    }

    public void setUnavailabilityHeliosStartDate(LocalDateTime unavailabilityHeliosStartDate) {
        this.unavailabilityHeliosStartDate = unavailabilityHeliosStartDate;
    }

    public LocalDateTime getUnavailabilityHeliosEndDate() {
        return unavailabilityHeliosEndDate;
    }

    public void setUnavailabilityHeliosEndDate(LocalDateTime unavailabilityHeliosEndDate) {
        this.unavailabilityHeliosEndDate = unavailabilityHeliosEndDate;
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

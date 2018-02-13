package fr.sictiam.stela.acteservice.model;

import com.fasterxml.jackson.annotation.JsonView;
import fr.sictiam.stela.acteservice.model.ui.Views;
import fr.sictiam.stela.acteservice.validation.EmailCollection;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Admin {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;

    @NotNull
    @Email
    private String mainEmail;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "additional_emails", joinColumns = @JoinColumn(name = "admin_uuid"))
    @Column(name = "additional_email")
    @EmailCollection
    private List<String> additionalEmails;

    @NotNull
    private boolean miatAvailable;
    @NotNull
    private LocalDateTime unavailabilityMiatStartDate;
    @NotNull
    private LocalDateTime unavailabilityMiatEndDate;
    @JsonView(Views.AdminAlertMessageView.class)
    private boolean alertMessageDisplayed;
    @JsonView(Views.AdminAlertMessageView.class)
    private String alertMessage;

    public Admin() {

    }

    public Admin(String uuid, String mainEmail, List<String> additionalEmails, boolean miatAvailable,
            LocalDateTime unavailabilityMiatStartDate, LocalDateTime unavailabilityMiatEndDate,
            boolean alertMessageDisplayed, String alertMessage) {
        this.uuid = uuid;
        this.mainEmail = mainEmail;
        this.additionalEmails = additionalEmails;
        this.miatAvailable = miatAvailable;
        this.unavailabilityMiatStartDate = unavailabilityMiatStartDate;
        this.unavailabilityMiatEndDate = unavailabilityMiatEndDate;
        this.alertMessageDisplayed = alertMessageDisplayed;
        this.alertMessage = alertMessage;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getMainEmail() {
        return mainEmail;
    }

    public void setMainEmail(String mainEmail) {
        this.mainEmail = mainEmail;
    }

    public List<String> getAdditionalEmails() {
        return additionalEmails;
    }

    public void setAdditionalEmails(List<String> additionalEmails) {
        this.additionalEmails = additionalEmails;
    }

    public boolean isMiatAvailable() {
        return miatAvailable;
    }

    public void setMiatAvailable(boolean miatAvailable) {
        this.miatAvailable = miatAvailable;
    }

    public LocalDateTime getUnavailabilityMiatStartDate() {
        return unavailabilityMiatStartDate;
    }

    public void setUnavailabilityMiatStartDate(LocalDateTime unavailabilityMiatStartDate) {
        this.unavailabilityMiatStartDate = unavailabilityMiatStartDate;
    }

    public LocalDateTime getUnavailabilityMiatEndDate() {
        return unavailabilityMiatEndDate;
    }

    public void setUnavailabilityMiatEndDate(LocalDateTime unavailabilityMiatEndDate) {
        this.unavailabilityMiatEndDate = unavailabilityMiatEndDate;
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

    @Override
    public String toString() {
        return "StelaInstanceInfo [uuid=" + uuid + ", mainEmail=" + mainEmail + ", additionalEmails=" + additionalEmails
                + "]";
    }

}

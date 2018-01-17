package fr.sictiam.stela.pesservice.model;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;

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

    public Admin() {

    }

    public Admin(String uuid, @NotNull boolean heliosAvailable, @NotNull LocalDateTime unavailabilityHeliosStartDate,
            @NotNull LocalDateTime unavailabilityHeliosEndDate) {
        this.uuid = uuid;
        this.heliosAvailable = heliosAvailable;
        this.unavailabilityHeliosStartDate = unavailabilityHeliosStartDate;
        this.unavailabilityHeliosEndDate = unavailabilityHeliosEndDate;
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

}

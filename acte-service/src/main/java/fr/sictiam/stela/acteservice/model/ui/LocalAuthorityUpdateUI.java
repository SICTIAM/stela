package fr.sictiam.stela.acteservice.model.ui;

import fr.sictiam.stela.acteservice.model.StampPosition;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class LocalAuthorityUpdateUI {
    // TODO add specific message or not
    @NotNull(message = "form.validation.mandatoryfield")
    @Pattern(regexp = "[\\d]{3}", message = "form.validation.malformedfield")
    private String department;
    @NotNull(message = "form.validation.mandatoryfield")
    @Pattern(regexp = "[\\d]{1}", message = "form.validation.malformedfield")
    private String district;
    @NotNull(message = "form.validation.mandatoryfield")
    @Pattern(regexp = "[\\d]{2}", message = "form.validation.malformedfield")
    private String nature;
    private Boolean canPublishRegistre;
    private Boolean canPublishWebSite;
    private StampPosition stampPosition;

    public LocalAuthorityUpdateUI() {
    }

    public String getDepartment() {
        return department;
    }

    public String getDistrict() {
        return district;
    }

    public String getNature() {
        return nature;
    }

    public Boolean getCanPublishRegistre() {
        return canPublishRegistre;
    }

    public Boolean getCanPublishWebSite() {
        return canPublishWebSite;
    }

    public StampPosition getStampPosition() {
        return stampPosition;
    }
}

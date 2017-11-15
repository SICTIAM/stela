package fr.sictiam.stela.acteservice.model.ui;


import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class LocalAuthorityUpdateUI {

    @NotNull @Pattern(regexp="[\\d]{3}")
    private String department;
    @NotNull @Pattern(regexp="[\\d]{1}")
    private String district;
    @NotNull @Pattern(regexp="[\\d]{2}")
    private String nature;
    private Boolean canPublishRegistre;
    private Boolean canPublishWebSite;

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
}

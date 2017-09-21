package fr.sictiam.stela.acteservice.model.ui;


public class LocalAuthorityUpdateUI {

    private String department;
    private String district;
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

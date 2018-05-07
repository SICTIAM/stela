package fr.sictiam.stela.acteservice.model;

import javax.persistence.Embeddable;

@Embeddable
public class ArchiveSettings {

    private boolean archiveActivated;
    private String pastellUrl;
    private String pastellEntity;
    private String pastellLogin;
    private String pastellPassword;
    private Integer daysBeforeArchiving;

    public ArchiveSettings() {
    }

    public ArchiveSettings(boolean archiveActivated, String pastellUrl, String pastellEntity, String pastellLogin,
            String pastellPassword, int daysBeforeArchiving) {
        this.archiveActivated = archiveActivated;
        this.pastellUrl = pastellUrl;
        this.pastellEntity = pastellEntity;
        this.pastellLogin = pastellLogin;
        this.pastellPassword = pastellPassword;
        this.daysBeforeArchiving = daysBeforeArchiving;
    }

    public boolean isArchiveActivated() {
        return archiveActivated;
    }

    public void setArchiveActivated(boolean archiveActivated) {
        this.archiveActivated = archiveActivated;
    }

    public String getPastellUrl() {
        return pastellUrl;
    }

    public void setPastellUrl(String pastellUrl) {
        this.pastellUrl = pastellUrl;
    }

    public String getPastellEntity() {
        return pastellEntity;
    }

    public void setPastellEntity(String pastellEntity) {
        this.pastellEntity = pastellEntity;
    }

    public String getPastellLogin() {
        return pastellLogin;
    }

    public void setPastellLogin(String pastellLogin) {
        this.pastellLogin = pastellLogin;
    }

    public String getPastellPassword() {
        return pastellPassword;
    }

    public void setPastellPassword(String pastellPassword) {
        this.pastellPassword = pastellPassword;
    }

    public Integer getDaysBeforeArchiving() {
        return daysBeforeArchiving;
    }

    public void setDaysBeforeArchiving(Integer daysBeforeArchiving) {
        this.daysBeforeArchiving = daysBeforeArchiving;
    }
}

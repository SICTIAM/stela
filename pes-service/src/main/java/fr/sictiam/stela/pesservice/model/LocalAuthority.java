package fr.sictiam.stela.pesservice.model;

import fr.sictiam.stela.pesservice.model.migration.Migration;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;

import java.util.List;

@Entity
public class LocalAuthority {

    @Id
    private String uuid;
    private String name;
    private String siren;
    @Enumerated(EnumType.STRING)
    private ServerCode serverCode;
    private Boolean active;
    private Boolean sesileSubscription;
    private Boolean sesileNewVersion;
    private String token;
    private String secret;
    private String genericProfileUuid;
    private ArchiveSettings archiveSettings;
    private String slugName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "sirens", joinColumns = @JoinColumn(name = "local_authority_uuid"))
    @Column(name = "siren")
    private List<String> sirens;
    private Migration migration;

    public LocalAuthority() {
    }

    public LocalAuthority(String uuid, String name, String siren, Boolean active) {
        this.uuid = uuid;
        this.name = name;
        this.siren = siren;
        this.active = active;
    }

    public LocalAuthority(String uuid, String name, String siren, String slugName) {
        this.uuid = uuid;
        this.name = name;
        this.siren = siren;
        this.slugName = slugName;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getSiren() {
        return siren;
    }

    public void setSiren(String siren) {
        this.siren = siren;
    }

    public ServerCode getServerCode() {
        return serverCode;
    }

    public void setServerCode(ServerCode serverCode) {
        this.serverCode = serverCode;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<String> getSirens() {
        return sirens;
    }

    public void setSirens(List<String> sirens) {
        this.sirens = sirens;
    }

    public Boolean getSesileSubscription() {
        return sesileSubscription;
    }

    public void setSesileSubscription(Boolean sesileSubscription) {
        this.sesileSubscription = sesileSubscription;
    }

    public Boolean getSesileNewVersion() {
        return sesileNewVersion;
    }

    public void setSesileNewVersion(Boolean sesileNewVersion) {
        this.sesileNewVersion = sesileNewVersion;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getGenericProfileUuid() {
        return genericProfileUuid;
    }

    public void setGenericProfileUuid(String genericProfileUuid) {
        this.genericProfileUuid = genericProfileUuid;
    }

    public Migration getMigration() {
        return migration;
    }

    public void setMigration(Migration migration) {
        this.migration = migration;
    }

    public ArchiveSettings getArchiveSettings() {
        return archiveSettings;
    }

    public void setArchiveSettings(ArchiveSettings archiveSettings) {
        this.archiveSettings = archiveSettings;
    }

    public String getSlugName() {
        return slugName;
    }

    public void setSlugName(String slugName) {
        this.slugName = slugName;
    }

    @Override
    public String toString() {
        return "LocalAuthority [uuid=" + uuid + ", name=" + name + ", slugName=" + slugName + ", siren=" + siren + ", serverCode=" + serverCode
                + ", active=" + active + ", sirens=" + sirens + "]";
    }
}

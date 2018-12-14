package fr.sictiam.stela.convocationservice.model;

import com.fasterxml.jackson.annotation.JsonView;
import fr.sictiam.stela.convocationservice.model.ui.Views;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class LocalAuthority {

    @Id
    @JsonView(Views.LocalAuthorityView.class)
    private String uuid;

    @JsonView(Views.LocalAuthorityView.class)
    private String name;

    @JsonView(Views.LocalAuthorityView.class)
    private String slugName;

    @JsonView(Views.LocalAuthorityView.class)
    private String siren;

    @JsonView(Views.LocalAuthorityView.class)
    private Boolean active;

    @JsonView(Views.LocalAuthorityView.class)
    private Long residentNumber;

    public LocalAuthority() {
    }

    public LocalAuthority(String uuid, String name, String slugName, String siren, Boolean active) {
        this.uuid = uuid;
        this.name = name;
        this.slugName = slugName;
        this.siren = siren;
        this.active = active;
    }

    public LocalAuthority(String uuid, String name, String slugName, String siren) {
        this(uuid, name, slugName, siren, true);
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

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Long getResidentNumber() {
        return residentNumber;
    }

    public void setResidentNumber(Long residentNumber) {
        this.residentNumber = residentNumber;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlugName() {
        return slugName;
    }

    public void setSlugName(String slugName) {
        this.slugName = slugName;
    }
}

package fr.sictiam.stela.convocationservice.model;

import com.fasterxml.jackson.annotation.JsonView;
import fr.sictiam.stela.convocationservice.model.ui.Views;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class LocalAuthority {

    @Id
    @JsonView(Views.Public.class)
    private String uuid;

    @JsonView(Views.Public.class)
    private String name;

    @JsonView(Views.Public.class)
    private String slugName;

    @JsonView(Views.LocalAuthority.class)
    private String siren;

    @JsonView(Views.LocalAuthority.class)
    private Boolean active;

    @Embedded
    @JsonView(Views.LocalAuthority.class)
    private StampPosition stampPosition;

    /**
     * true if inhabitant number is > 3500, false otherwise
     * Used for legal convocation delay
     */
    @JsonView(Views.LocalAuthority.class)
    private Boolean residentThreshold = true;

    @OneToOne(cascade = CascadeType.ALL)
    @JsonView(Views.LocalAuthority.class)
    private Attachment defaultProcuration;

    public LocalAuthority() {
    }

    public LocalAuthority(String uuid, String name, String slugName, String siren, Boolean active, StampPosition stampPosition) {
        this.uuid = uuid;
        this.name = name;
        this.slugName = slugName;
        this.siren = siren;
        this.active = active;
        this.stampPosition = stampPosition;
    }

    public LocalAuthority(String uuid, String name, String slugName, String siren, StampPosition stampPosition) {
        this(uuid, name, slugName, siren, true, stampPosition);
    }

    public LocalAuthority(String uuid, String name, String slugName, String siren) {
        this(uuid, name, slugName, siren, true, new StampPosition());
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

    public Boolean getResidentThreshold() {
        return residentThreshold;
    }

    public void setResidentThreshold(Boolean residentThreshold) {
        this.residentThreshold = residentThreshold;
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

    public StampPosition getStampPosition() {
        return stampPosition;
    }

    public void setStampPosition(StampPosition stampPosition) {
        this.stampPosition = stampPosition;
    }

    public Attachment getDefaultProcuration() {
        return defaultProcuration;
    }

    public void setDefaultProcuration(Attachment defaultProcuration) {
        this.defaultProcuration = defaultProcuration;
    }
}

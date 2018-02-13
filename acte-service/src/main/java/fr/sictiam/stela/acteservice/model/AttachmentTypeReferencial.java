package fr.sictiam.stela.acteservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import java.util.Set;

@Entity
public class AttachmentTypeReferencial {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;

    private ActeNature acteNature;

    @OneToMany(mappedBy = "attachmentTypeReferencial", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<AttachmentType> attachmentTypes;

    @ManyToOne
    @JsonIgnore
    private LocalAuthority localAuthority;

    public AttachmentTypeReferencial() {
    }

    public AttachmentTypeReferencial(ActeNature acteNature, Set<AttachmentType> attachmentTypes,
            LocalAuthority localAuthority) {
        this.acteNature = acteNature;
        this.attachmentTypes = attachmentTypes;
        this.localAuthority = localAuthority;
    }

    public ActeNature getActeNature() {
        return acteNature;
    }

    public void setActeNature(ActeNature acteNature) {
        this.acteNature = acteNature;
    }

    public Set<AttachmentType> getAttachmentTypes() {
        return attachmentTypes;
    }

    public void setAttachmentTypes(Set<AttachmentType> attachmentTypes) {
        this.attachmentTypes = attachmentTypes;
    }

    public LocalAuthority getLocalAuthority() {
        return localAuthority;
    }
}

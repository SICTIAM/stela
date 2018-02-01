package fr.sictiam.stela.pesservice.model;

import java.time.LocalDateTime;
import java.util.SortedSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import fr.sictiam.stela.pesservice.config.LocalDateDeserializer;

@Entity
public class PesAller {

    public interface RestValidation {
        // validation group marker interface
    }

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
    
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDateTime creation;
   
    @Column(length = 512)
    @NotNull(groups = { RestValidation.class })
    @Size(max = 500)
    private String objet;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Attachment attachment;  
   
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("date ASC")
    private SortedSet<PesHistory> pesHistories;
   
    @ManyToOne
    private LocalAuthority localAuthority;

    private String profileUuid;
    
    private String comment;
    
    private boolean Pj;
    
    private boolean signed;

    public PesAller() {
    }

    public String getUuid() {
        return this.uuid;
    }

    public String getObjet() {
        return objet;
    }

    public void setObjet(String objet) {
        this.objet = objet;
    }

    public LocalDateTime getCreation() {
        return creation;
    }

    public void setCreation(LocalDateTime creation) {
        this.creation = creation;
    }

    public SortedSet<PesHistory> getPesHistories() {
        return pesHistories;
    }

    public void setPesHistories(SortedSet<PesHistory> pesHistories) {
        this.pesHistories = pesHistories;
    }

    public LocalAuthority getLocalAuthority() {
        return localAuthority;
    }

    public void setLocalAuthority(LocalAuthority localAuthority) {
        this.localAuthority = localAuthority;
    }

    public String getProfileUuid() {
        return profileUuid;
    }

    public void setProfileUuid(String profileUuid) {
        this.profileUuid = profileUuid;
    }
    
    public Attachment getAttachment() {
        return attachment;
    }

    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isPj() {
        return Pj;
    }

    public void setPj(boolean pj) {
        this.Pj = pj;
    }

    public boolean isSigned() {
        return signed;
    }

    public void setSigned(boolean signed) {
        this.signed = signed;
    }
}

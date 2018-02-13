package fr.sictiam.stela.pesservice.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.sictiam.stela.pesservice.config.LocalDateDeserializer;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import java.time.LocalDateTime;

@Entity
public class PesRetour {

    public interface RestValidation {
        // validation group marker interface
    }

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDateTime creation;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Attachment attachment;

    @ManyToOne
    private LocalAuthority localAuthority;

    public PesRetour(Attachment attachment, LocalAuthority localAuthority) {
        this.creation = LocalDateTime.now();
        this.attachment = attachment;
        this.localAuthority = localAuthority;
    }

    public PesRetour() {
    }

    public String getUuid() {
        return this.uuid;
    }

    public LocalDateTime getCreation() {
        return creation;
    }

    public void setCreation(LocalDateTime creation) {
        this.creation = creation;
    }

    public LocalAuthority getLocalAuthority() {
        return localAuthority;
    }

    public void setLocalAuthority(LocalAuthority localAuthority) {
        this.localAuthority = localAuthority;
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }
}

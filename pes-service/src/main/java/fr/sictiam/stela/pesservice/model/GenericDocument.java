package fr.sictiam.stela.pesservice.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.sictiam.stela.pesservice.config.LocalDateDeserializer;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import java.time.LocalDateTime;

/**
 * GenericDocument
 * <p>
 * This class is used to store the link between a sesileClasseurId with a sesileDocumentId for metier applications that
 * will call (formerly PAULL) Stela to get infos on a classeur.
 * It is also used as a trace
 */

@Entity
public class GenericDocument {

    @Id
    private Integer sesileClasseurId;

    private Integer sesileDocumentId;

    private Integer serviceOrganisationNumber;

    private String depositEmail;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDateTime creation;

    @ManyToOne
    private LocalAuthority localAuthority;

    public GenericDocument(Integer sesileClasseurId, Integer sesileDocumentId, Integer serviceOrganisationNumber,
            String depositEmail, LocalDateTime creation, LocalAuthority localAuthority) {
        this.sesileClasseurId = sesileClasseurId;
        this.sesileDocumentId = sesileDocumentId;
        this.serviceOrganisationNumber = serviceOrganisationNumber;
        this.depositEmail = depositEmail;
        this.creation = creation;
        this.localAuthority = localAuthority;
    }

    public GenericDocument() {
    }

    public Integer getSesileClasseurId() {
        return sesileClasseurId;
    }

    public void setSesileClasseurId(Integer sesileClasseurId) {
        this.sesileClasseurId = sesileClasseurId;
    }

    public Integer getSesileDocumentId() {
        return sesileDocumentId;
    }

    public Integer getServiceOrganisationNumber() {
        return serviceOrganisationNumber;
    }

    public void setServiceOrganisationNumber(Integer serviceOrganisationNumber) {
        this.serviceOrganisationNumber = serviceOrganisationNumber;
    }

    public void setSesileDocumentId(Integer sesileDocumentId) {
        this.sesileDocumentId = sesileDocumentId;
    }

    public String getDepositEmail() {
        return depositEmail;
    }

    public void setDepositEmail(String depositEmail) {
        this.depositEmail = depositEmail;
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

}

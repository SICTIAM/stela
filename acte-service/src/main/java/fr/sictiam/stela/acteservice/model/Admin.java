package fr.sictiam.stela.acteservice.model;

import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.Email;

import fr.sictiam.stela.acteservice.validation.EmailCollection;

@Entity
public class Admin {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;

    @NotNull
    @Email
    private String mainEmail;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "additional_emails", joinColumns = @JoinColumn(name = "admin_uuid"))
    @Column(name = "additional_email")
    @EmailCollection
    private List<String> additionalEmails;

    public Admin() {

    }

    public Admin(String uuid, String mainEmail, List<String> additionalEmails) {
	super();
	this.uuid = uuid;
	this.mainEmail = mainEmail;
	this.additionalEmails = additionalEmails;
    }

    public String getUuid() {
	return uuid;
    }

    public void setUuid(String uuid) {
	this.uuid = uuid;
    }

    public String getMainEmail() {
	return mainEmail;
    }

    public void setMainEmail(String mainEmail) {
	this.mainEmail = mainEmail;
    }

    public List<String> getAdditionalEmails() {
	return additionalEmails;
    }

    public void setAdditionalEmails(List<String> additionalEmails) {
	this.additionalEmails = additionalEmails;
    }

    @Override
    public String toString() {
	return "StelaInstanceInfo [uuid=" + uuid + ", mainEmail=" + mainEmail + ", additionalEmails=" + additionalEmails
		+ "]";
    }

}

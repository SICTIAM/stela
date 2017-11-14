package fr.sictiam.stela.acteservice.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.List;

@Entity
public class Admin {

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	private String uuid;
	private String mainEmail;
	
	@ElementCollection
	@CollectionTable(name="additional_emails", joinColumns=@JoinColumn(name="admin_uuid"))
	@Column(name="additional_email")
	private List<String> additionalEmails;

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
		return "StelaInstanceInfo [uuid=" + uuid + ", mainEmail=" + mainEmail + ", additionalEmails="
				+ additionalEmails + "]";
	}

}

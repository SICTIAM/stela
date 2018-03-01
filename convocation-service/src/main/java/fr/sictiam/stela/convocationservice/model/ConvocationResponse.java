package fr.sictiam.stela.convocationservice.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import java.util.Set;

@Entity
public class ConvocationResponse {

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	private String uuid;

	private String profileUuid;

	@ManyToOne
	private ExternalUser externalUser;

	private String substituteProfileUuid;

	@ManyToOne
	private ExternalUser substituteExternalUser;

	@ManyToOne
	private Convocation convocation;

	@Enumerated(EnumType.STRING)
	private ResponseType responseType;

	@OneToMany
	private Set<QuestionResponse> questionResponses;

	public String getProfileUuid() {
		return profileUuid;
	}

	public void setProfileUuid(String profileUuid) {
		this.profileUuid = profileUuid;
	}

	public ExternalUser getExternalUser() {
		return externalUser;
	}

	public void setExternalUser(ExternalUser externalUser) {
		this.externalUser = externalUser;
	}

	public String getSubstituteProfileUuid() {
		return substituteProfileUuid;
	}

	public void setSubstituteProfileUuid(String substituteProfileUuid) {
		this.substituteProfileUuid = substituteProfileUuid;
	}

	public ExternalUser getSubstituteExternalUser() {
		return substituteExternalUser;
	}

	public void setSubstituteExternalUser(ExternalUser substituteExternalUser) {
		this.substituteExternalUser = substituteExternalUser;
	}

	public Convocation getConvocation() {
		return convocation;
	}

	public void setConvocation(Convocation convocation) {
		this.convocation = convocation;
	}

	public ResponseType getResponseType() {
		return responseType;
	}

	public void setResponseType(ResponseType responseType) {
		this.responseType = responseType;
	}

	public Set<QuestionResponse> getQuestionResponses() {
		return questionResponses;
	}

	public void setQuestionResponses(Set<QuestionResponse> questionResponses) {
		this.questionResponses = questionResponses;
	}

	public String getUuid() {
		return uuid;
	}
}

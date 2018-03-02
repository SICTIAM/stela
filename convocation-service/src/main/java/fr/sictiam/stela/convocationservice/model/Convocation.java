package fr.sictiam.stela.convocationservice.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.sictiam.stela.convocationservice.config.LocalDateTimeDeserializer;
import fr.sictiam.stela.convocationservice.model.ui.Views;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
public class Convocation {

	public interface RestValidation {
		// validation group marker interface
	}

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	@JsonView(Views.ConvocationViewPublic.class)
	private String uuid;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "observer_profile_uuids", joinColumns = @JoinColumn(name = "convocation_uuid"))
	@Column(name = "profile_uuid")
	@JsonView(Views.ConvocationViewPublic.class)
	private Set<String> observerProfileUuids;

	@OneToMany(fetch = FetchType.EAGER)
	@JsonView(Views.ConvocationViewPrivate.class)
	private Set<ExternalUser> externalObserver;

	@ManyToOne
	@JsonView(Views.ConvocationViewPrivate.class)
	private AssemblyType assemblyType;

	@ManyToOne
	@JsonView(Views.ConvocationViewPublic.class)
	private Attachment attachment;

	@OneToMany
	@JsonView(Views.ConvocationViewPublic.class)
	private Set<Attachment> annexes;

	@OneToMany
	@JsonView(Views.ConvocationViewPublic.class)
	private Set<Question> questions;

	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@JsonView(Views.ConvocationViewPublic.class)
	private LocalDateTime creationDate;

	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@JsonView(Views.ConvocationViewPublic.class)
	private LocalDateTime meetingDate;

	@JsonView(Views.ConvocationViewPublic.class)
	private String place;

	@JsonView(Views.ConvocationViewPublic.class)
	private String subject;

	@JsonView(Views.ConvocationViewPublic.class)
	private String comment;

	@JsonView(Views.ConvocationViewPublic.class)
	private String profileUuid;

	@JsonView(Views.ConvocationViewPublic.class)
	private String groupUuid;

	public String getUuid() {
		return uuid;
	}

	public Set<String> getObserverProfileUuids() {
		return observerProfileUuids;
	}

	public void setObserverProfileUuids(Set<String> observerProfileUuids) {
		this.observerProfileUuids = observerProfileUuids;
	}

	public Set<ExternalUser> getExternalObserver() {
		return externalObserver;
	}

	public void setExternalObserver(Set<ExternalUser> externalObserver) {
		this.externalObserver = externalObserver;
	}

	public AssemblyType getAssemblyType() {
		return assemblyType;
	}

	public void setAssemblyType(AssemblyType assemblyType) {
		this.assemblyType = assemblyType;
	}

	public Attachment getAttachment() {
		return attachment;
	}

	public void setAttachment(Attachment attachment) {
		this.attachment = attachment;
	}

	public Set<Attachment> getAnnexes() {
		return annexes;
	}

	public void setAnnexes(Set<Attachment> annexes) {
		this.annexes = annexes;
	}

	public Set<Question> getQuestions() {
		return questions;
	}

	public void setQuestions(Set<Question> questions) {
		this.questions = questions;
	}

	public LocalDateTime getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(LocalDateTime creationDate) {
		this.creationDate = creationDate;
	}

	public LocalDateTime getMeetingDate() {
		return meetingDate;
	}

	public void setMeetingDate(LocalDateTime meetingDate) {
		this.meetingDate = meetingDate;
	}

	public String getPlace() {
		return place;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getProfileUuid() {
		return profileUuid;
	}

	public void setProfileUuid(String profileUuid) {
		this.profileUuid = profileUuid;
	}

	public String getGroupUuid() {
		return groupUuid;
	}

	public void setGroupUuid(String groupUuid) {
		this.groupUuid = groupUuid;
	}

}

package fr.sictiam.stela.convocationservice.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.sictiam.stela.convocationservice.config.LocalDateTimeDeserializer;
import fr.sictiam.stela.convocationservice.model.ui.Views;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.SortedSet;

@Entity
public class Convocation {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JsonView(Views.Convocation.class)
    private String uuid;

    @NotNull
    @ManyToMany(fetch = FetchType.EAGER)
    @JsonView(Views.ConvocationInternal.class)
    private Set<Recipient> recipients;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JsonView(Views.Convocation.class)
    private AssemblyType assemblyType;

    @OneToOne(cascade = CascadeType.ALL)
    @JsonView(Views.Convocation.class)
    private Attachment attachment;

    @OneToMany(cascade = CascadeType.ALL)
    @JsonView(Views.Convocation.class)
    private Set<Attachment> annexes;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonView(Views.Convocation.class)
    private Set<Question> questions;

    @OneToMany(mappedBy = "convocation")
    @JsonView(Views.ConvocationInternal.class)
    private Set<RecipientResponse> recipientResponses;

    @OneToMany
    @JsonView(Views.ConvocationInternal.class)
    @OrderBy("date ASC")
    private SortedSet<ConvocationHistory> histories;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    //@JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonView(Views.Convocation.class)
    private LocalDateTime creationDate;

    @NotNull
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    //@JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonView(Views.Convocation.class)
    private LocalDateTime meetingDate;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    //@JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonView(Views.Convocation.class)
    private LocalDateTime sentDate;

    @ManyToOne
    @JsonView(Views.ConvocationInternal.class)
    private LocalAuthority localAuthority;

    @NotNull
    @JsonView(Views.Convocation.class)
    private String location;

    @NotNull
    @JsonView(Views.Convocation.class)
    private String subject;

    @JsonView(Views.Convocation.class)
    private String comment;

    @JsonView(Views.Convocation.class)
    private String profileUuid;

    public Convocation() {
    }

    public String getUuid() {
        return uuid;
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

    public Set<Recipient> getRecipients() {
        return recipients;
    }

    public void setRecipients(Set<Recipient> recipients) {
        this.recipients = recipients;
    }

    public Set<RecipientResponse> getRecipientResponses() {
        return recipientResponses;
    }

    public void setRecipientResponses(Set<RecipientResponse> recipientResponses) {
        this.recipientResponses = recipientResponses;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public SortedSet<ConvocationHistory> getHistories() {
        return histories;
    }

    public void setHistories(SortedSet<ConvocationHistory> histories) {
        this.histories = histories;
    }

    public LocalAuthority getLocalAuthority() {
        return localAuthority;
    }

    public void setLocalAuthority(LocalAuthority localAuthority) {
        this.localAuthority = localAuthority;
    }

    public LocalDateTime getSentDate() {
        return sentDate;
    }

    public void setSentDate(LocalDateTime sentDate) {
        this.sentDate = sentDate;
    }

    @Override public String toString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writerWithView(Views.ConvocationInternal.class).writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "[ uuid: " + uuid +
                    ", subject: " + subject +
                    ", comment: " + comment +
                    ", location: " + location +
                    ", creation: " + creationDate +
                    ", meetingDate: " + meetingDate + "]";
        }
    }
}

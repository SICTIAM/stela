package fr.sictiam.stela.convocationservice.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.sictiam.stela.convocationservice.config.LocalDateTimeDeserializer;
import fr.sictiam.stela.convocationservice.model.ui.Views;
import fr.sictiam.stela.convocationservice.model.util.RecipientResponseComparator;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.SortComparator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

@Entity
public class Convocation {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JsonView(Views.Convocation.class)
    private String uuid;


    @ManyToOne(fetch = FetchType.EAGER)
    @JsonView(Views.Convocation.class)
    private AssemblyType assemblyType;

    @OneToOne(cascade = CascadeType.ALL)
    @JsonView(Views.ConvocationInternal.class)
    private Attachment attachment;

    @OneToOne(cascade = CascadeType.ALL)
    @JsonView(Views.ConvocationInternal.class)
    private Attachment procuration;

    @OneToMany(cascade = CascadeType.ALL)
    @JsonView(Views.ConvocationInternal.class)
    private Set<Attachment> annexes;

    @OneToOne(cascade = CascadeType.ALL)
    @JsonView(Views.ConvocationInternal.class)
    private Attachment minutes;


    @OneToMany(cascade = CascadeType.ALL)
    @JsonView(Views.ConvocationInternal.class)
    @OrderBy("rank ASC")
    private SortedSet<Question> questions;

    @OneToMany(mappedBy = "convocation", fetch = FetchType.EAGER)
    @JsonView(Views.ConvocationReceived.class)
    @SortComparator(RecipientResponseComparator.class)
    private SortedSet<RecipientResponse> recipientResponses;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "convocation", fetch = FetchType.EAGER)
    @JsonView(Views.ConvocationInternal.class)
    @OrderBy("date DESC")
    private SortedSet<ConvocationHistory> histories;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonView(Views.Convocation.class)
    private LocalDateTime creationDate;

    @NotNull
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonView(Views.Convocation.class)
    private LocalDateTime meetingDate;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
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

    @JsonView(Views.Convocation.class)
    private boolean cancelled = false;

    @JsonView(Views.Convocation.class)
    private LocalDateTime cancellationDate;

    @JsonView(Views.Convocation.class)
    private String groupUuid;

    @Transient
    @JsonView(Views.Convocation.class)
    private Profile profile;

    @Transient
    private Set<Recipient> recipients;


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

    public void setQuestions(SortedSet<Question> questions) {
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

    public SortedSet<RecipientResponse> getRecipientResponses() {
        if (recipientResponses == null)
            recipientResponses = new TreeSet<>();
        return recipientResponses;
    }

    public void setRecipientResponses(SortedSet<RecipientResponse> recipientResponses) {
        this.recipientResponses = recipientResponses;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public SortedSet<ConvocationHistory> getHistories() {
        if (histories == null)
            histories = new TreeSet<>();

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

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public LocalDateTime getCancellationDate() {
        return cancellationDate;
    }

    public void setCancellationDate(LocalDateTime cancellationDate) {
        this.cancellationDate = cancellationDate;
    }

    public Attachment getProcuration() {
        return procuration;
    }

    public void setProcuration(Attachment procuration) {
        this.procuration = procuration;
    }

    public Attachment getMinutes() {
        return minutes;
    }

    public void setMinutes(Attachment minutes) {
        this.minutes = minutes;
    }

    public String getGroupUuid() {
        return groupUuid;
    }

    public void setGroupUuid(String groupUuid) {
        this.groupUuid = groupUuid;
    }

    @Override public String toString() {
        return "{" +
                "\"uuid\": " + uuid + '\'' +
                ", \"assemblyType\": \"" + assemblyType +
                ", \"attachment\": \"" + attachment +
                ", \"procuration\": \"" + procuration +
                ", \"annexes\": \"" + annexes +
                ", \"minutes\": \"" + minutes +
                ", \"questions\": \"" + questions +
                ", \"recipientResponses\": \"" + recipientResponses +
                ", \"histories\": \"" + histories +
                ", \"creationDate\": \"" + creationDate +
                ", \"meetingDate\": \"" + meetingDate +
                ", \"sentDate\": \"" + sentDate +
                ", \"localAuthority\": \"" + localAuthority +
                ", \"location\": \"'" + location + '\'' +
                ", \"subject\": \"'" + subject + '\'' +
                ", \"comment\": \"'" + comment + '\'' +
                ", \"profileUuid\": \"'" + profileUuid + '\'' +
                ", \"cancelled\": \"" + cancelled +
                ", \"cancellationDate\": \"" + cancellationDate +
                ", \"profile\": \"" + profile +
                ", \"recipients\": \"" + recipients +
                '}';
    }
}

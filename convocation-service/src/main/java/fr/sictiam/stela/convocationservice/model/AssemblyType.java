package fr.sictiam.stela.convocationservice.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fr.sictiam.stela.convocationservice.config.LocalDateTimeDeserializer;
import fr.sictiam.stela.convocationservice.config.LocalDateTimeSerializer;
import fr.sictiam.stela.convocationservice.model.ui.Views;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import java.time.LocalDateTime;
import java.util.Set;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "uuid")
@Entity
public class AssemblyType {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JsonView(Views.AssemblyTypeViewPublic.class)
    private String uuid;

    @JsonView(Views.AssemblyTypeViewPublic.class)
    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "assembly_type_recipient",
            joinColumns = @JoinColumn(name = "assembly_type_uuid"),
            inverseJoinColumns = @JoinColumn(name = "recipient_uuid"))
    @JsonView(Views.AssemblyTypeViewPublic.class)
    private Set<Recipient> recipients;

    @JsonView(Views.AssemblyTypeViewPublic.class)
    private int delay;

    @JsonView(Views.AssemblyTypeViewPublic.class)
    private int reminderDelay;

    @JsonView(Views.AssemblyTypeViewPublic.class)
    private String location;

    @JsonView(Views.AssemblyTypeViewPublic.class)
    private boolean useProcuration;

    @JsonView(Views.AssemblyTypeViewPublic.class)
    private boolean active;

    @JsonView(Views.UserViewPublic.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    LocalDateTime inactivityDate;

    @JsonView(Views.AssemblyTypeViewPublic.class)
    private String profileUuid;

    @ManyToOne
    @JsonView(Views.AssemblyTypeViewPrivate.class)
    private LocalAuthority localAuthority;

    public AssemblyType(String name, Set<Recipient> recipients,
            LocalAuthority localAuthority) {
        this.name = name;
        this.recipients = recipients;
        this.localAuthority = localAuthority;
    }


    public AssemblyType() {
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Recipient> getRecipients() {
        return recipients;
    }

    public void setRecipients(Set<Recipient> recipients) {
        this.recipients = recipients;
    }

    public LocalAuthority getLocalAuthority() {
        return localAuthority;
    }

    public void setLocalAuthority(LocalAuthority localAuthority) {
        this.localAuthority = localAuthority;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getReminderDelay() {
        return reminderDelay;
    }

    public void setReminderDelay(int reminderDelay) {
        this.reminderDelay = reminderDelay;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isUseProcuration() {
        return useProcuration;
    }

    public void setUseProcuration(boolean useProcuration) {
        this.useProcuration = useProcuration;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getInactivityDate() {
        return inactivityDate;
    }

    public void setInactivityDate(LocalDateTime inactivityDate) {
        this.inactivityDate = inactivityDate;
    }

    public String getProfileUuid() {
        return profileUuid;
    }

    public void setProfileUuid(String profileUuid) {
        this.profileUuid = profileUuid;
    }
}

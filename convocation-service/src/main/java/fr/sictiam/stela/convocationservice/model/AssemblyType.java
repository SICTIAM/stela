package fr.sictiam.stela.convocationservice.model;

import com.fasterxml.jackson.annotation.JsonView;
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
import javax.persistence.OrderBy;

import java.time.LocalDateTime;
import java.util.SortedSet;
import java.util.TreeSet;

@Entity
public class AssemblyType {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JsonView(Views.Public.class)
    private String uuid;

    @JsonView(Views.Public.class)
    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "assembly_type_recipient",
            joinColumns = @JoinColumn(name = "assembly_type_uuid"),
            inverseJoinColumns = @JoinColumn(name = "recipient_uuid"))
    @JsonView(Views.AssemblyTypeInternal.class)
    @OrderBy("lastname,firstname,email ASC")
    private SortedSet<Recipient> recipients = new TreeSet<>();

    @JsonView(Views.AssemblyType.class)
    private Integer delay;

    @JsonView(Views.AssemblyType.class)
    private Boolean reminder;

    @JsonView(Views.AssemblyType.class)
    private String location;

    @JsonView(Views.AssemblyType.class)
    private Boolean useProcuration;

    @JsonView(Views.AssemblyType.class)
    private Boolean active;

    @JsonView(Views.AssemblyType.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime inactivityDate;

    @JsonView(Views.AssemblyType.class)
    private String profileUuid;

    @ManyToOne
    @JsonView(Views.AssemblyType.class)
    private LocalAuthority localAuthority;

    public AssemblyType(String name, SortedSet<Recipient> recipients,
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

    public SortedSet<Recipient> getRecipients() {
        return recipients;
    }

    public void setRecipients(SortedSet<Recipient> recipients) {
        this.recipients = recipients;
    }

    public LocalAuthority getLocalAuthority() {
        return localAuthority;
    }

    public void setLocalAuthority(LocalAuthority localAuthority) {
        this.localAuthority = localAuthority;
    }

    public Integer getDelay() {
        return delay;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    public Boolean getReminder() {
        return reminder;
    }

    public void setReminder(Boolean reminder) {
        this.reminder = reminder;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Boolean getUseProcuration() {
        return useProcuration;
    }

    public void setUseProcuration(Boolean useProcuration) {
        this.useProcuration = useProcuration;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
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

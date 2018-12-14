package fr.sictiam.stela.convocationservice.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;
import fr.sictiam.stela.convocationservice.model.ui.Views;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Entity
public class AssemblyType {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JsonView(Views.AssemblyTypeViewPublic.class)
    private String uuid;

    @JsonView(Views.AssemblyTypeViewPublic.class)
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "profile_uuids", joinColumns = @JoinColumn(name = "assembly_type_uuid"))
    @Column(name = "profile_uuid")
    @JsonView(Views.AssemblyTypeViewPublic.class)
    private Set<String> profileUuids;

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

    @JsonView(Views.AssemblyTypeViewPublic.class)
    @Transient
    private Map<String, JsonNode> profiles;

    public AssemblyType(String name, Set<String> profileUuids, Set<Recipient> recipients,
            LocalAuthority localAuthority) {
        this.name = name;
        this.profileUuids = profileUuids;
        this.recipients = recipients;
        this.localAuthority = localAuthority;
    }

    @ManyToOne
    @JsonView(Views.AssemblyTypeViewPrivate.class)
    private LocalAuthority localAuthority;

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

    public Set<String> getProfileUuids() {
        return profileUuids;
    }

    public void setProfileUuids(Set<String> profileUuids) {
        this.profileUuids = profileUuids;
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

    public Map<String, JsonNode> getProfiles() {
        return profiles;
    }

    public void setProfiles(Map<String, JsonNode> profiles) {
        this.profiles = profiles;
    }

    public void addProfile(String uuid, JsonNode profile) {
        if (profiles == null)
            profiles = new HashMap<>();

        profiles.put(uuid, profile);
    }

    public interface Light {

        public String getUuid();

        public String getName();

    }
}

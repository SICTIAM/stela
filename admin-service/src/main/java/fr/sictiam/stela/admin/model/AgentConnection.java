package fr.sictiam.stela.admin.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import java.time.LocalDateTime;

@Entity
public class AgentConnection {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;

    private LocalDateTime connectionDate;

    @ManyToOne(fetch = FetchType.EAGER)
    private Profile profile;

    public AgentConnection(Profile profile) {
        this.profile = profile;
        this.connectionDate = LocalDateTime.now();
    }

    public String getUuid() {
        return uuid;
    }

    public LocalDateTime getConnectionDate() {
        return connectionDate;
    }

    public void setConnectionDate(LocalDateTime connectionDate) {
        this.connectionDate = connectionDate;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

}

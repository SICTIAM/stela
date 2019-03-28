package fr.sictiam.stela.convocationservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import fr.sictiam.stela.convocationservice.model.ui.Views;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import java.util.Objects;
import java.util.Set;

@Entity
public class Tag {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JsonView(Views.Tag.class)
    private String uuid;

    @Column(length = 32)
    @JsonView(Views.Tag.class)
    private String name;

    @Column(length = 8)
    @JsonView(Views.Tag.class)
    private String color;

    @Column(length = 128)
    @JsonView(Views.Tag.class)
    private String icon;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "local_authority_uuid")
    private LocalAuthority localAuthority;

    @JsonIgnore
    @ManyToMany(mappedBy = "tags")
    private Set<Attachment> attachments;


    public Tag() {
    }

    public Tag(String name, String color, String icon, LocalAuthority localAuthority) {
        this.name = name;
        this.color = color;
        this.icon = icon;
        this.localAuthority = localAuthority;
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public LocalAuthority getLocalAuthority() {
        return localAuthority;
    }

    public void setLocalAuthority(LocalAuthority localAuthority) {
        this.localAuthority = localAuthority;
    }

    public Set<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(Set<Attachment> attachments) {
        this.attachments = attachments;
    }

    @Override public String toString() {
        return "{" +
                "\"uuid\": \"" + uuid + "\"" +
                ", \"name\": \"'" + name + "\"" +
                ", \"color\": \"" + color +
                ", \"icon\": \"'" + icon + "\"" +
                '}';
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return Objects.equals(uuid, tag.uuid);
    }
}

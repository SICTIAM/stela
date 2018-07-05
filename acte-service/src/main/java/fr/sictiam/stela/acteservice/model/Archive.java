package fr.sictiam.stela.acteservice.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Archive {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;

    @Enumerated(EnumType.STRING)
    private ArchiveStatus status;
    private String archiveUrl;
    private String asalaeDocumentId;

    public Archive() {
    }

    public Archive(String asalaeDocumentId) {
        this.asalaeDocumentId = asalaeDocumentId;
    }

    public String getUuid() {
        return uuid;
    }

    public ArchiveStatus getStatus() {
        return status;
    }

    public void setStatus(ArchiveStatus status) {
        this.status = status;
    }

    public String getArchiveUrl() {
        return archiveUrl;
    }

    public void setArchiveUrl(String archiveUrl) {
        this.archiveUrl = archiveUrl;
    }

    public String getAsalaeDocumentId() {
        return asalaeDocumentId;
    }

    public void setAsalaeDocumentId(String asalaeDocumentId) {
        this.asalaeDocumentId = asalaeDocumentId;
    }
}

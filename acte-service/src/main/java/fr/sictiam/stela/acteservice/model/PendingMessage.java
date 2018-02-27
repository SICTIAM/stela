package fr.sictiam.stela.acteservice.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class PendingMessage {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;

    private String acteUuid;

    private byte[] file;

    private String fileName;

    @Enumerated(EnumType.STRING)
    private Flux flux;

    public PendingMessage() {
    }

    public PendingMessage(ActeHistory acteHistory) {
        this.acteUuid = acteHistory.getActeUuid();
        this.file = acteHistory.getFile();
        this.fileName = acteHistory.getFileName();
        this.flux = acteHistory.getFlux();
    }

    public String getActeUuid() {
        return acteUuid;
    }

    public byte[] getFile() {
        return file;
    }

    public String getFileName() {
        return fileName;
    }

    public Flux getFlux() {
        return flux;
    }

}

package fr.sictiam.stela.acteservice.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.sictiam.stela.acteservice.config.LocalDateDeserializer;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Acte {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
    @Column(unique=true)
    private String number;
    private LocalDateTime creation;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate decision;
    private ActeNature nature;
    private LocalDateTime lastUpdateTime;
    private String code;
    private String title;
    private boolean isPublic;
    private boolean isPublicWebsite;
    private StatusType status;
    private byte[] file;
    private String filename;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Attachment> annexes;

    public Acte() {
    }

    public Acte(String number, LocalDate decision, ActeNature nature, String code, String title, boolean isPublic, boolean isPublicWebsite) {
        this.number = number;
        this.decision = decision;
        this.nature = nature;
        this.code = code;
        this.title = title;
        this.isPublic = isPublic;
        this.isPublicWebsite = isPublicWebsite;
    }

    public String getUuid() {
        return this.uuid;
    }

    public String getNumber() {
        return this.number;
    }

    public LocalDate getDecision() {
        return decision;
    }

    public ActeNature getNature() {
        return nature;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public boolean isPublicWebsite() {
        return isPublicWebsite;
    }

    public void setPublicWebsite(boolean publicWebsite) {
        isPublicWebsite = publicWebsite;
    }

    public LocalDateTime getCreation() {
        return creation;
    }

    public void setCreation(LocalDateTime creation) {
        this.creation = creation;
    }

    public StatusType getStatus() {
        return status;
    }

    public void setStatus(StatusType status) {
        this.status = status;
    }

    public LocalDateTime getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(LocalDateTime lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public byte[] getFile(){
        return this.file;
    }

    public void setFile(byte[] file){
        this.file = file;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public List<Attachment> getAnnexes() {
        return annexes;
    }

    public void setAnnexes(List<Attachment> annexes) {
        this.annexes = annexes;
    }

    @Override
    public String toString() {
        return "Acte{" +
                "uuid:" + uuid +
                ", number:" + number + '\'' +
                ", decision:" + decision +
                ", nature:" + nature +
                ", code:'" + code + '\'' +
                ", title:'" + title + '\'' +
                ", isPublic:" + isPublic +
                ", creation:" + creation +
                ", status:" + status +
                ", lastUpdateTime:" + lastUpdateTime +
                ", file name:" + filename +
                '}';
    }
}

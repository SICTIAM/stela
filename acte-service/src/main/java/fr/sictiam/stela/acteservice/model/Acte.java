package fr.sictiam.stela.acteservice.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class Acte {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;

    @Column(unique=true)
    private String number;

    @JsonFormat(pattern="dd/MM/yyyy - HH:mm")
    private Date creation;

    @JsonFormat(pattern="yyyy-MM-dd")
    private Date decision;

    private ActeNature nature;

    @JsonFormat(pattern="dd/MM/yyyy - HH:mm")
    private Date lastUpdateTime;

    private String code;

    private String title;

    private boolean isPublic;
    
    private StatusType status;

    String file;

    public Acte() {
    }

    public Acte(String number, Date decision, ActeNature nature, String code, String title, boolean isPublic) {
        this.number = number;
        this.decision = decision;
        this.nature = nature;
        this.code = code;
        this.title = title;
        this.isPublic = isPublic;
    }

    public String getUuid() {
        return this.uuid;
    }

    public String getNumber() {
        return this.number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Date getDecision() {
        return decision;
    }

    public void setDecision(Date decision) {
        this.decision = decision;
    }

    public ActeNature getNature() {
        return nature;
    }

    public void setNature(ActeNature nature) {
        this.nature = nature;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Date getCreation() {
        return creation;
    }

    public void setCreation(Date creation) {
        this.creation = creation;
    }

    public StatusType getStatus() {
        return status;
    }

    public void setStatus(StatusType status) {
        this.status = status;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getFile(){
        return this.file;
    }
    
    public void setFile(String file){
        this.file = file;
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
                ", file name:" + file +
                '}';
    }
}

package fr.sictiam.stela.acteservice.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.sictiam.stela.acteservice.config.LocalDateDeserializer;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.SortedSet;

@Entity
public class Acte {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
    @Column(unique=true)
    @NotNull @Max(15) @Pattern(regexp = "/^[a-zA-Z0-9_]+$/")
    private String number;
    private LocalDateTime creation;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @NotNull
    private LocalDate decision;
    @NotNull
    private ActeNature nature;
    @NotNull
    private String code;
    private String codeLabel;
    @Column(length = 512)
    @NotNull @Max(500)
    private String objet;
    private boolean isPublic;
    private boolean isPublicWebsite;
    private byte[] file;
    private String filename;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Attachment> annexes;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("date ASC")
    private SortedSet<ActeHistory> acteHistories;
    @ManyToOne
    private LocalAuthority localAuthority;

    public Acte() {
    }

    public Acte(String number, LocalDate decision, ActeNature nature, String code, String objet, boolean isPublic, boolean isPublicWebsite) {
        this.number = number;
        this.decision = decision;
        this.nature = nature;
        this.code = code;
        this.objet = objet;
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

    public String getCodeLabel() {
        return codeLabel;
    }

    public void setCodeLabel(String codeLabel) {
        this.codeLabel = codeLabel;
    }

    public String getObjet() {
        return objet;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public boolean getIsPublicWebsite() {
        return isPublicWebsite;
    }

    public void setPublicWebsite(boolean isPublicWebsite) {
        this.isPublicWebsite = isPublicWebsite;
    }

    public LocalDateTime getCreation() {
        return creation;
    }

    public void setCreation(LocalDateTime creation) {
        this.creation = creation;
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

    public SortedSet<ActeHistory> getActeHistories() {
        return acteHistories;
    }

    public void setActeHistories(SortedSet<ActeHistory> acteHistories) {
        this.acteHistories = acteHistories;
    }

    public LocalAuthority getLocalAuthority() {
        return localAuthority;
    }

    public void setLocalAuthority(LocalAuthority localAuthority) {
        this.localAuthority = localAuthority;
    }

    @Override
    public String toString() {
        return "Acte{" +
                "uuid:" + uuid +
                ", number:" + number + '\'' +
                ", decision:" + decision +
                ", nature:" + nature +
                ", code:'" + code + '\'' +
                ", objet:'" + objet + '\'' +
                ", isPublic:" + isPublic +
                ", isPublicWebsite:" + isPublicWebsite +
                ", creation:" + creation +
                ", filename:" + filename +
                ", localAuthority:" + localAuthority +
                '}';
    }
}

package fr.sictiam.stela.acteservice.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.sictiam.stela.acteservice.config.LocalDateDeserializer;
import org.apache.commons.lang.StringUtils;
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

    interface RestValidation {
        // validation group marker interface
    }

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
    @NotNull(groups = {RestValidation.class}) @Max(value=15, groups = {RestValidation.class}) @Pattern(regexp = "/^[a-zA-Z0-9_]+$/", groups = {RestValidation.class})
    private String number;
    private LocalDateTime creation;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @NotNull(groups = {RestValidation.class})
    private LocalDate decision;
    @NotNull(groups = {RestValidation.class})
    private ActeNature nature;
    @NotNull(groups = {RestValidation.class})
    private String code;
    private String codeLabel;
    @Column(length = 512)
    @NotNull(groups = {RestValidation.class}) @Max(value=500, groups = {RestValidation.class})
    private String objet;
    private boolean isPublic;
    private boolean isPublicWebsite;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Attachment acteAttachment;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Attachment> annexes;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("date ASC")
    private SortedSet<ActeHistory> acteHistories;
    @ManyToOne
    private LocalAuthority localAuthority;
    private boolean draft;

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

    public void setObjet(String objet) {
        this.objet = objet;
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

    public Attachment getActeAttachment(){
        return this.acteAttachment;
    }

    public void setActeAttachment(Attachment acteAttachment){
        this.acteAttachment = acteAttachment;
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

    public boolean isDraft() {
        return draft;
    }

    public void setDraft(boolean draft) {
        this.draft = draft;
    }

    public boolean empty() {
        return StringUtils.isEmpty(number)
                && decision == null
                && nature == null
                && StringUtils.isEmpty(code)
                && StringUtils.isEmpty(codeLabel)
                && StringUtils.isEmpty(objet)
                && acteAttachment == null
                && (annexes == null || annexes.size() == 0)
                && (acteHistories == null || acteHistories.size() == 0);

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
                ", localAuthority:" + localAuthority +
                '}';
    }
}

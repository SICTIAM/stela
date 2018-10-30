package fr.sictiam.stela.acteservice.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fr.sictiam.stela.acteservice.config.LocalDateDeserializer;
import fr.sictiam.stela.acteservice.config.LocalDateSerializer;
import fr.sictiam.stela.acteservice.config.LocalDateTimeSerializer;
import fr.sictiam.stela.acteservice.model.ui.Views;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

@Entity
public class Acte {

    public interface RestValidation {
        // validation group marker interface
    }

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JsonView(Views.ActePublicView.class)
    private String uuid;
    @NotNull(groups = {RestValidation.class})
    @Size(max = 15, groups = {RestValidation.class})
    @Pattern(regexp = "^[A-Z0-9_]+$", groups = {RestValidation.class})
    @JsonView(Views.ActePublicView.class)
    private String number;
    @JsonView(Views.ActePublicView.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime creation;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @NotNull(groups = {RestValidation.class})
    @JsonView(Views.ActePublicView.class)
    private LocalDate decision;
    @NotNull(groups = {RestValidation.class})
    @JsonView(Views.ActePublicView.class)
    private ActeNature nature;
    @NotNull(groups = {RestValidation.class})
    @JsonView(Views.ActePublicView.class)
    private String code;
    @JsonView(Views.ActePublicView.class)
    private String codeLabel;
    @Column(length = 512)
    @NotNull(groups = {RestValidation.class})
    @Size(max = 500)
    @JsonView(Views.ActePublicView.class)
    private String objet;
    @JsonView(Views.ActePublicView.class)
    private boolean isPublic;
    @JsonView(Views.ActePublicView.class)
    private boolean isPublicWebsite;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonView(Views.ActePublicView.class)
    private Attachment acteAttachment;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonView(Views.ActePublicView.class)
    private List<Attachment> annexes;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("date ASC")
    @JsonView(Views.ActePublicView.class)
    private SortedSet<ActeHistory> acteHistories;
    @ManyToOne
    @JsonView(Views.ActePublicView.class)
    private LocalAuthority localAuthority;
    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private Draft draft;

    private String profileUuid;

    private String groupUuid;

    public String getMiatId() {
        return miatId;
    }

    @Formula("(select concat(la.department,'-',la.siren, '-',to_char(creation, 'YYYYMMdd'),'-', number,'-', CASE WHEN nature = '0' THEN 'DE' "
            + " WHEN nature = '1' THEN 'AR' " + " WHEN nature = '2' THEN 'AI' " + " WHEN nature = '3' THEN 'CC' "
            + " WHEN nature = '4' THEN 'BF' "
            + " WHEN nature = '5' THEN 'AU' ELSE '' END)  from local_authority la where la.uuid=local_authority_uuid)")
    private String miatId;
    private boolean imported;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Archive archive;

    @Enumerated(EnumType.STRING)
    StatusType lastHistoryStatus;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    LocalDateTime lastHistoryDate;
    @Enumerated(EnumType.STRING)
    Flux lastHistoryFlux;

    public Acte() {
    }

    public Acte(String uuid, String objet, LocalDateTime creation, LocalDate decision, String number, ActeNature nature, LocalDateTime lastHistoryDate, StatusType lastHistoryStatus, Flux lastHistoryFlux) {
        this.uuid = uuid;
        this.objet = objet;
        this.creation = creation;
        this.decision = decision;
        this.number = number;
        this.nature = nature;
        this.lastHistoryDate = lastHistoryDate;
        this.lastHistoryStatus = lastHistoryStatus;
        this.lastHistoryFlux = lastHistoryFlux;
    }

    public Acte(String number, LocalDate decision, ActeNature nature, String code, String objet, boolean isPublic,
            boolean isPublicWebsite) {
        this.number = number;
        this.decision = decision;
        this.nature = nature;
        this.code = code;
        this.objet = objet;
        this.isPublic = isPublic;
        this.isPublicWebsite = isPublicWebsite;
    }

    public Acte(String number, LocalDateTime creation, LocalDate decision, ActeNature nature, String code,
            String codeLabel, String objet, boolean isPublic, boolean isPublicWebsite, Attachment acteAttachment,
            List<Attachment> annexes, SortedSet<ActeHistory> acteHistories, LocalAuthority localAuthority, String groupUuid,
            String profileUuid, boolean imported) {
        this.number = number;
        this.creation = creation;
        this.decision = decision;
        this.nature = nature;
        this.code = code;
        this.codeLabel = codeLabel;
        this.objet = objet;
        this.isPublic = isPublic;
        this.isPublicWebsite = isPublicWebsite;
        this.acteAttachment = acteAttachment;
        this.annexes = annexes;
        this.acteHistories = acteHistories;
        this.localAuthority = localAuthority;
        this.groupUuid = groupUuid;
        this.profileUuid = profileUuid;
        this.imported = imported;
    }

    public Acte(String number, String objet, ActeNature nature, String code, LocalDateTime creation, LocalDate decision,
            Boolean isPublic, Boolean isPublicWebsite, String groupUuid, Attachment acteAttachment,
            List<Attachment> annexes, String profileUuid, LocalAuthority localAuthority) {
        this.number = number;
        this.creation = creation;
        this.decision = decision;
        this.nature = nature;
        this.code = code;
        this.objet = objet;
        this.isPublic = isPublic;
        this.isPublicWebsite = isPublicWebsite;
        this.groupUuid = groupUuid;
        this.acteAttachment = acteAttachment;
        this.annexes = annexes;
        this.acteHistories = Collections.emptySortedSet();
        this.profileUuid = profileUuid;
        this.localAuthority = localAuthority;
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

    public LocalDate getDecision() {
        return decision;
    }

    public void setDecision(LocalDate decision) {
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

    public boolean isPublicWebsite() {
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

    public Attachment getActeAttachment() {
        return this.acteAttachment;
    }

    public void setActeAttachment(Attachment acteAttachment) {
        this.acteAttachment = acteAttachment;
    }

    public List<Attachment> getAnnexes() {
        return annexes != null ? annexes : new ArrayList<>();
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

    public Draft getDraft() {
        return draft;
    }

    public void setDraft(Draft draft) {
        this.draft = draft;
    }

    public String getProfileUuid() {
        return profileUuid;
    }

    public void setProfileUuid(String profileUuid) {
        this.profileUuid = profileUuid;
    }

    public String getGroupUuid() {
        return groupUuid;
    }

    public void setGroupUuid(String groupUuid) {
        this.groupUuid = groupUuid;
    }

    public boolean isImported() {
        return imported;
    }

    public void setImported(boolean imported) {
        this.imported = imported;
    }

    public Archive getArchive() {
        return archive;
    }

    public void setArchive(Archive archive) {
        this.archive = archive;
    }

    public StatusType getLastHistoryStatus() {
        return lastHistoryStatus;
    }

    public void setLastHistoryStatus(StatusType lastHistoryStatus) {
        this.lastHistoryStatus = lastHistoryStatus;
    }

    public LocalDateTime getLastHistoryDate() {
        return lastHistoryDate;
    }

    public void setLastHistoryDate(LocalDateTime lastHistoryDate) {
        this.lastHistoryDate = lastHistoryDate;
    }

    public Flux getLastHistoryFlux() {
        return lastHistoryFlux;
    }

    public void setLastHistoryFlux(Flux lastHistoryFlux) {
        this.lastHistoryFlux = lastHistoryFlux;
    }

    public boolean empty() {
        return StringUtils.isEmpty(number) && decision == null && nature == null && StringUtils.isEmpty(code)
                && StringUtils.isEmpty(codeLabel) && StringUtils.isEmpty(objet) && acteAttachment == null
                && (annexes == null || annexes.size() == 0) && (acteHistories == null || acteHistories.size() == 0);

    }

    @Override
    public String toString() {
        return "Acte{" + "uuid:" + uuid + ", number:" + number + '\'' + ", decision:" + decision + ", nature:" + nature
                + ", code:'" + code + '\'' + ", objet:'" + objet + '\'' + ", isPublic:" + isPublic
                + ", isPublicWebsite:" + isPublicWebsite + ", creation:" + creation + ", localAuthority:"
                + localAuthority + '}';
    }
}

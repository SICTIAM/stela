package fr.sictiam.stela.pesservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.sictiam.stela.pesservice.config.LocalDateDeserializer;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.URL;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.SortedSet;
import java.util.TreeSet;

@Entity
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class PesAller {

    public interface RestValidation {
        // validation group marker interface
    }

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDateTime creation;

    @Column(length = 512)
    @NotNull(groups = { RestValidation.class })
    @Size(max = 500, groups = { RestValidation.class })
    private String objet;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Attachment attachment;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("date ASC")
    private SortedSet<PesHistory> pesHistories;

    @ManyToOne
    private LocalAuthority localAuthority;

    private String profileUuid;

    @Size(max = 250, groups = { RestValidation.class })
    private String comment;

    private String fileType;
    private String colCode;
    private String postId;
    private String budCode;
    private String fileName;

    private boolean pj;

    private boolean signed;

    private Integer sesileClasseurId;

    private Integer sesileDocumentId;

    @URL
    private String sesileClasseurUrl;

    private LocalDate validationLimit;

    private Integer serviceOrganisationNumber;

    private boolean imported;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Archive archive;

    private LocalDateTime lastHistoryDate;
    @Enumerated(EnumType.STRING)
    private StatusType lastHistoryStatus;

    public PesAller() {
    }

    public PesAller(String uuid, LocalDateTime creation, String objet, String fileType, LocalDateTime lastHistoryDate,
            StatusType lastHistoryStatus) {
        this.uuid = uuid;
        this.creation = creation;
        this.objet = objet;
        this.fileType = fileType;
        this.lastHistoryDate = lastHistoryDate;
        this.lastHistoryStatus = lastHistoryStatus;
    }

    public PesAller(LocalDateTime creation, String objet, Attachment attachment, SortedSet<PesHistory> pesHistories,
            LocalAuthority localAuthority, String profileUuid, String comment, String fileType, String colCode,
            String postId, String budCode, String fileName, boolean pj, boolean signed, Integer sesileDocumentId,
            boolean imported) {
        this.creation = creation;
        this.objet = objet;
        this.attachment = attachment;
        this.pesHistories = pesHistories;
        this.localAuthority = localAuthority;
        this.profileUuid = profileUuid;
        this.comment = comment;
        this.fileType = fileType;
        this.colCode = colCode;
        this.postId = postId;
        this.budCode = budCode;
        this.fileName = fileName;
        this.pj = pj;
        this.signed = signed;
        this.sesileDocumentId = sesileDocumentId;
        this.imported = imported;
    }

    public String getUuid() {
        return uuid;
    }

    public String getObjet() {
        return objet;
    }

    public void setObjet(String objet) {
        this.objet = objet;
    }

    public LocalDateTime getCreation() {
        return creation;
    }

    public void setCreation(LocalDateTime creation) {
        this.creation = creation;
    }

    public SortedSet<PesHistory> getPesHistories() {
        return pesHistories != null ? pesHistories : new TreeSet<>();
    }

    public void setPesHistories(SortedSet<PesHistory> pesHistories) {
        this.pesHistories = pesHistories;
    }

    public LocalAuthority getLocalAuthority() {
        return localAuthority;
    }

    public void setLocalAuthority(LocalAuthority localAuthority) {
        this.localAuthority = localAuthority;
    }

    public String getProfileUuid() {
        return profileUuid;
    }

    public void setProfileUuid(String profileUuid) {
        this.profileUuid = profileUuid;
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getColCode() {
        return colCode;
    }

    public void setColCode(String colCode) {
        this.colCode = colCode;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getBudCode() {
        return budCode;
    }

    public void setBudCode(String budCode) {
        this.budCode = budCode;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isPj() {
        return pj;
    }

    public void setPj(boolean pj) {
        this.pj = pj;
    }

    public boolean isSigned() {
        return signed;
    }

    public Integer getSesileClasseurId() {
        return sesileClasseurId;
    }

    public void setSesileClasseurId(Integer sesileClasseurId) {
        this.sesileClasseurId = sesileClasseurId;
    }

    public void setSigned(boolean signed) {
        this.signed = signed;
    }

    public Integer getSesileDocumentId() {
        return sesileDocumentId;
    }

    public String getSesileClasseurUrl() {
        return sesileClasseurUrl;
    }

    public void setSesileClasseurUrl(String sesileClasseurUrl) {
        this.sesileClasseurUrl = sesileClasseurUrl;
    }

    public LocalDate getValidationLimit() {
        return validationLimit;
    }

    public void setValidationLimit(LocalDate validationLimit) {
        this.validationLimit = validationLimit;
    }

    public Integer getServiceOrganisationNumber() {
        return serviceOrganisationNumber;
    }

    public void setServiceOrganisationNumber(Integer serviceOrganisationNumber) {
        this.serviceOrganisationNumber = serviceOrganisationNumber;
    }

    public void setSesileDocumentId(Integer sesileDocumentId) {
        this.sesileDocumentId = sesileDocumentId;
    }

    public boolean isImported() {
        return imported;
    }

    public void setImported(boolean imported) {
        this.imported = imported;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Archive getArchive() {
        return archive;
    }

    public void setArchive(Archive archive) {
        this.archive = archive;
    }

    public LocalDateTime getLastHistoryDate() {
        return lastHistoryDate;
    }

    public void setLastHistoryDate(LocalDateTime lastHistoryDate) {
        this.lastHistoryDate = lastHistoryDate;
    }

    public StatusType getLastHistoryStatus() {
        return lastHistoryStatus;
    }

    public void setLastHistoryStatus(StatusType lastHistoryStatus) {
        this.lastHistoryStatus = lastHistoryStatus;
    }

    public interface Light {
        String getUuid();

        String getObjet();

        LocalDateTime getCreation();

        LocalAuthority getLocalAuthority();

        boolean isPj();

        String getProfileUuid();

        SortedSet<PesHistory> getPesHistories();

        Integer getSesileClasseurId();

        Integer getSesileDocumentId();

        LocalDateTime getLastHistoryDate();

        StatusType getLastHistoryStatus();
    }

}

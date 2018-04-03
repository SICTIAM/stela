package fr.sictiam.stela.acteservice.model.migration;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ActeMigration {

    private String form_id;
    private String number;
    private String objet;
    private LocalDateTime creation;
    private LocalDateTime sendDate;
    private LocalDate decision;
    private String code_matiere1;
    private String code_matiere2;
    private String code_matiere3;
    private String code_matiere4;
    private String code_matiere5;
    private String code_label;
    private boolean is_public;
    private boolean is_public_website;
    private String nature;
    private String archivePath;
    private String acteAttachment;
    private String annexes;
    private String status;
    private LocalDateTime dateAR;
    private String archivePathAR;
    private String filenameAR;
    private LocalDateTime dateANO;
    private String archivePathANO;
    private String filenameANO;
    private String messageANO;
    private LocalDateTime dateASKCANCEL;
    private String archivePathASKCANCEL;
    private String filenameASKCANCEL;
    private LocalDateTime dateARCANCEL;
    private String archivePathARCANCEL;
    private String filenameARCANCEL;

    public ActeMigration() {
    }

    public ActeMigration(String form_id, String number, String objet, LocalDateTime creation, LocalDateTime sendDate,
            LocalDate decision, String code_matiere1, String code_matiere2, String code_matiere3, String code_matiere4,
            String code_matiere5, String code_label, boolean is_public, boolean is_public_website, String nature,
            String archivePath, String acteAttachment, String annexes, String status, LocalDateTime dateAR,
            String archivePathAR, String filenameAR, LocalDateTime dateANO, String archivePathANO, String filenameANO,
            String messageANO, LocalDateTime dateASKCANCEL, String archivePathASKCANCEL, String filenameASKCANCEL,
            LocalDateTime dateARCANCEL, String archivePathARCANCEL, String filenameARCANCEL) {
        this.form_id = form_id;
        this.number = number;
        this.objet = objet;
        this.creation = creation;
        this.sendDate = sendDate;
        this.decision = decision;
        this.code_matiere1 = code_matiere1;
        this.code_matiere2 = code_matiere2;
        this.code_matiere3 = code_matiere3;
        this.code_matiere4 = code_matiere4;
        this.code_matiere5 = code_matiere5;
        this.code_label = code_label;
        this.is_public = is_public;
        this.is_public_website = is_public_website;
        this.nature = nature;
        this.archivePath = archivePath;
        this.acteAttachment = acteAttachment;
        this.annexes = annexes;
        this.status = status;
        this.dateAR = dateAR;
        this.archivePathAR = archivePathAR;
        this.filenameAR = filenameAR;
        this.dateANO = dateANO;
        this.archivePathANO = archivePathANO;
        this.filenameANO = filenameANO;
        this.messageANO = messageANO;
        this.dateASKCANCEL = dateASKCANCEL;
        this.archivePathASKCANCEL = archivePathASKCANCEL;
        this.filenameASKCANCEL = filenameASKCANCEL;
        this.dateARCANCEL = dateARCANCEL;
        this.archivePathARCANCEL = archivePathARCANCEL;
        this.filenameARCANCEL = filenameARCANCEL;
    }

    public String getForm_id() {
        return form_id;
    }

    public String getNumber() {
        return number;
    }

    public String getObjet() {
        return objet;
    }

    public LocalDateTime getCreation() {
        return creation;
    }

    public LocalDateTime getSendDate() {
        return sendDate;
    }

    public LocalDate getDecision() {
        return decision;
    }

    public String getCode_matiere1() {
        return code_matiere1;
    }

    public String getCode_matiere2() {
        return code_matiere2;
    }

    public String getCode_matiere3() {
        return code_matiere3;
    }

    public String getCode_matiere4() {
        return code_matiere4;
    }

    public String getCode_matiere5() {
        return code_matiere5;
    }

    public String getCode_label() {
        return code_label;
    }

    public boolean isIs_public() {
        return is_public;
    }

    public boolean isIs_public_website() {
        return is_public_website;
    }

    public String getNature() {
        return nature;
    }

    public String getArchivePath() {
        return archivePath;
    }

    public String getActeAttachment() {
        return acteAttachment;
    }

    public String getAnnexes() {
        return annexes;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getDateAR() {
        return dateAR;
    }

    public String getArchivePathAR() {
        return archivePathAR;
    }

    public String getFilenameAR() {
        return filenameAR;
    }

    public LocalDateTime getDateANO() {
        return dateANO;
    }

    public String getArchivePathANO() {
        return archivePathANO;
    }

    public String getFilenameANO() {
        return filenameANO;
    }

    public String getMessageANO() {
        return messageANO;
    }

    public LocalDateTime getDateASKCANCEL() {
        return dateASKCANCEL;
    }

    public String getArchivePathASKCANCEL() {
        return archivePathASKCANCEL;
    }

    public String getFilenameASKCANCEL() {
        return filenameASKCANCEL;
    }

    public LocalDateTime getDateARCANCEL() {
        return dateARCANCEL;
    }

    public String getArchivePathARCANCEL() {
        return archivePathARCANCEL;
    }

    public String getFilenameARCANCEL() {
        return filenameARCANCEL;
    }
}

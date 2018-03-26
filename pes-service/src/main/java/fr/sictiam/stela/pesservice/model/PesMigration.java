package fr.sictiam.stela.pesservice.model;

import java.time.LocalDateTime;

public class PesMigration {

    private String message_id;
    private String objet;
    private String comment;
    private LocalDateTime creation;
    private String file_type;
    private String col_code;
    private String post_id;
    private String bud_code;
    private String file_name;
    private String pesAttachment;
    private long pesAttachmentSize;
    private String archivePath;
    private LocalDateTime sendDate;
    private String status;
    private LocalDateTime dateAR;
    private String filenameAR;
    private LocalDateTime dateANO;
    private String messageANO;
    private String filenameANO;
    private String pathFilenameANO;

    public PesMigration() {
    }

    public PesMigration(String message_id, String objet, String comment, LocalDateTime creation, String file_type, String col_code,
            String post_id, String bud_code, String file_name, String pesAttachment, long pesAttachmentSize, String archivePath,
            LocalDateTime sendDate, String status, LocalDateTime dateAR, String filenameAR, LocalDateTime dateANO, String messageANO,
            String filenameANO, String pathFilenameANO) {
        this.message_id = message_id;
        this.objet = objet;
        this.comment = comment;
        this.creation = creation;
        this.file_type = file_type;
        this.col_code = col_code;
        this.post_id = post_id;
        this.bud_code = bud_code;
        this.file_name = file_name;
        this.pesAttachment = pesAttachment;
        this.pesAttachmentSize = pesAttachmentSize;
        this.archivePath = archivePath;
        this.sendDate = sendDate;
        this.status = status;
        this.dateAR = dateAR;
        this.filenameAR = filenameAR;
        this.dateANO = dateANO;
        this.messageANO = messageANO;
        this.filenameANO = filenameANO;
        this.pathFilenameANO = pathFilenameANO;
    }

    public String getMessage_id() {
        return message_id;
    }

    public String getObjet() {
        return objet;
    }

    public String getComment() {
        return comment;
    }

    public LocalDateTime getCreation() {
        return creation;
    }

    public String getFile_type() {
        return file_type;
    }

    public String getCol_code() {
        return col_code;
    }

    public String getPost_id() {
        return post_id;
    }

    public String getBud_code() {
        return bud_code;
    }

    public String getFile_name() {
        return file_name;
    }

    public String getPesAttachment() {
        return pesAttachment;
    }

    public long getPesAttachmentSize() {
        return pesAttachmentSize;
    }

    public String getArchivePath() {
        return archivePath;
    }

    public LocalDateTime getSendDate() {
        return sendDate;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getDateAR() {
        return dateAR;
    }

    public String getFilenameAR() {
        return filenameAR;
    }

    public LocalDateTime getDateANO() {
        return dateANO;
    }

    public String getMessageANO() {
        return messageANO;
    }

    public String getFilenameANO() {
        return filenameANO;
    }

    public String getPathFilenameANO() {
        return pathFilenameANO;
    }

    @Override
    public String toString() {
        return "PesMigration{" +
                "objet='" + objet + '\'' +
                ", comment='" + comment + '\'' +
                ", creation='" + creation + '\'' +
                ", file_type='" + file_type + '\'' +
                ", col_code='" + col_code + '\'' +
                ", post_id='" + post_id + '\'' +
                ", bud_code='" + bud_code + '\'' +
                ", file_name='" + file_name + '\'' +
                ", pesAttachment='" + pesAttachment + '\'' +
                ", archivePath='" + archivePath + '\'' +
                ", sendDate='" + sendDate + '\'' +
                ", status='" + status + '\'' +
                ", dateAR='" + dateAR + '\'' +
                ", filenameAR='" + filenameAR + '\'' +
                ", dateANO='" + dateANO + '\'' +
                ", messageANO='" + messageANO + '\'' +
                ", pathFilenameANO='" + pathFilenameANO + '\'' +
                '}';
    }
}

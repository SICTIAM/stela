package fr.sictiam.stela.pes.dgfip.model.event;

import javax.persistence.Entity;

@Entity
public class PesCreatedEvent extends PesEvent {

    private String title;
    private String fileContent;
    private String fileName;
    private String comment;
    private Integer groupId;
    private Integer userId;

    public PesCreatedEvent() {
    }

    public PesCreatedEvent(String pesId, String title, String fileContent, String fileName, String comment, Integer groupId, Integer userId) {
        super(pesId, EventType.CREATED);

        this.title = title;
        this.fileContent = fileContent;
        this.fileName = fileName;
        this.comment = comment;
        this.groupId = groupId;
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}

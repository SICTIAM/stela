package fr.sictiam.stela.pes.dgfip.model;

public class Pes {
    private String id;
    private String pesId;
    private String title;
    private String fileContent;
    private String fileName;
    private String comment;
    private Integer groupId;
    private Integer userId;

    public Pes() {
    }

    public Pes(String pesId, String title, String fileContent, String fileName, String comment, Integer groupId, Integer userId) {

        this.pesId = pesId;
        this.title = title;
        this.fileContent = fileContent;
        this.fileName = fileName;
        this.comment = comment;
        this.groupId = groupId;
        this.userId = userId;
    }

    public String getId() {
        return id;
    }
    public String getPesId() {
        return pesId;
    }
    public String getTitle() {
        return title;
    }
    public String getFileContent() {
        return fileContent;
    }
    public String getFileName() {
        return fileName;
    }
    public String getComment() {
        return comment;
    }
    public Integer getGroupId() {
        return groupId;
    }
    public Integer getUserId() {
        return userId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPesId(String pesId) {
        this.pesId = pesId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Pes{" +
                "id='" + id + '\'' +
                ", pesId='" + pesId + '\'' +
                ", title='" + title + '\'' +
                ", fileContent='" + fileContent + '\'' +
                ", fileName='" + fileName + '\'' +
                ", comment='" + comment + '\'' +
                ", groupId=" + groupId +
                ", userId=" + userId +
                '}';
    }
}

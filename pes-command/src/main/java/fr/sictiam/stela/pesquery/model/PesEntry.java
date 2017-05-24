package fr.sictiam.stela.pesquery.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class PesEntry {

    @Id
    @GeneratedValue
    private long id;
    private String pesId;
    private String title;
    private String fileContent;
    private String fileName;
    private String comment;
    private Integer groupId;
    private Integer userId;
    private String dateEnvoi;
    public PesEntry() {
    }

    public PesEntry(String pesId, String title, String fileContent, String fileName, String comment, Integer groupId, Integer userId) {

        this.pesId = pesId;
        this.title = title;
        this.fileContent = fileContent;
        this.fileName = fileName;
        this.comment = comment;
        this.groupId = groupId;
        this.userId = userId;
    }

    public long getId() {
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
    public String getDateEnvoi() {
        return dateEnvoi;
    }

    public void setId(long id) {
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

    public void setDateEnvoi(String dateEnvoi) {
        this.dateEnvoi = dateEnvoi;
    }
}

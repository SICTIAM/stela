package fr.sictiam.stela.pesquery.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

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
}

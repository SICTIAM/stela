package fr.sictiam.stela.pescommand.command;

import org.axonframework.commandhandling.TargetAggregateIdentifier;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.UUID;

public class CreatePesCommand {

    @TargetAggregateIdentifier
    private String id;

    private String title;
    private String fileContent;
    private String fileName;
    private String comment;
    private Integer groupId;
    private Integer userId;

    public CreatePesCommand() {
        this.id = UUID.randomUUID().toString();
    }

    public CreatePesCommand(String id, String title, String fileContent, String fileName, String comment, Integer groupId, Integer userId) {
        this.id = id;
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

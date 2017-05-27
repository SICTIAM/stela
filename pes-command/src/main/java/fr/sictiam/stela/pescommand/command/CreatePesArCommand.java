package fr.sictiam.stela.pescommand.command;

import org.axonframework.commandhandling.TargetAggregateIdentifier;

import java.util.UUID;

public class CreatePesArCommand {

    @TargetAggregateIdentifier
    private String id;

    private String fileContent;
    private String fileName;


    public CreatePesArCommand() {
        this.id = UUID.randomUUID().toString();
    }

    public CreatePesArCommand(String id,  String fileContent, String fileName) {
        this.id = id;

        this.fileContent = fileContent;
        this.fileName = fileName;

    }

    public String getId() {
        return id;
    }

    public String getFileContent() {
        return fileContent;
    }
    public String getFileName() {
        return fileName;
    }

}

package fr.sictiam.stela.pescommand.event;

import java.util.UUID;

public class PesArCreatedEvent {

    private String id;

    private String fileContent;
    private String fileName;


    public PesArCreatedEvent() {
        this.id = UUID.randomUUID().toString();
    }

    public PesArCreatedEvent(String id, String fileContent, String fileName) {

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

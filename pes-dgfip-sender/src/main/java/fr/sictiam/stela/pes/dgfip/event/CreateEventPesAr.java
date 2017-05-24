package fr.sictiam.stela.pes.dgfip.event;

import java.util.UUID;

public class CreateEventPesAr {

    private String id;
    private String fileContent;
    private String fileName;

    public CreateEventPesAr () {
    }

    public CreateEventPesAr(String id,String fileContent, String fileName) {

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

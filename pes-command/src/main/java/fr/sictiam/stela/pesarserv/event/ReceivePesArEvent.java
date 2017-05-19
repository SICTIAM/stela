package fr.sictiam.stela.pesarserv.event;

import java.util.UUID;

/**
 * Created by s.vergon on 18/05/2017.
 */
public class ReceivePesArEvent {

    private String id;
    private String fileContent;
    private String fileName;


    public ReceivePesArEvent() {
        this.id = UUID.randomUUID().toString();
    }

    public ReceivePesArEvent(String id,String fileContent, String fileName) {

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

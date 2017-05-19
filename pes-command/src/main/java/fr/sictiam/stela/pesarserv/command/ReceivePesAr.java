package fr.sictiam.stela.pesarserv.command;

import org.axonframework.commandhandling.TargetAggregateIdentifier;

import java.util.UUID;

/**
 * Created by s.vergon on 18/05/2017.
 */
public class ReceivePesAr {

    @TargetAggregateIdentifier
    private String id;
    private String fileContent;
    private String fileName;
    public ReceivePesAr() {
        this.id = UUID.randomUUID().toString();
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

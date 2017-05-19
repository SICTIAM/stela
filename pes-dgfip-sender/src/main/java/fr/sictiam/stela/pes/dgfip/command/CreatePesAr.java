package fr.sictiam.stela.pes.dgfip.command;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

import java.util.UUID;

/**
 * Created by s.vergon on 16/05/2017.
 */
public class CreatePesAr {
    @TargetAggregateIdentifier
    private String id;
    private String fileContent;
    private String fileName;
    public CreatePesAr() {
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

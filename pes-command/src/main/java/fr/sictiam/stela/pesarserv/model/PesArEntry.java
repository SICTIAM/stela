package fr.sictiam.stela.pesarserv.model;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by s.vergon on 18/05/2017.
 */
public class PesArEntry {
    @Id
    @GeneratedValue
    private long id;
    private String pesarId;
    private String fileContent;
    private String fileName;

    public PesArEntry() {
    }

    public PesArEntry(String pesarId, String fileContent, String fileName) {

        this.pesarId = pesarId;
        this.fileContent = fileContent;
        this.fileName = fileName;
    }

    public long getId() {
        return id;
    }

    public String getPesarId() {
        return pesarId;
    }
    public String getFileContent() {
        return fileContent;
    }
    public String getFileName() {
        return fileName;
    }

}

package fr.sictiam.stela.pes.dgfip.model.event;

public class PesArReceivedEvent extends PesEvent {

    private String fileContent;
    private String fileName;

    public PesArReceivedEvent() {
    }

    public PesArReceivedEvent(String pesId, String fileContent, String fileName) {
        super(pesId, EventType.AR_RECEIVED);

        this.fileContent = fileContent;
        this.fileName = fileName;
    }

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}

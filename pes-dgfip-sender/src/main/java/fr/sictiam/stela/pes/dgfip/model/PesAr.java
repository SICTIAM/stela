package fr.sictiam.stela.pes.dgfip.model;

public class PesAr {

    private String fileContent;
    private String fileName;

    public PesAr() {
    }

    public PesAr(String fileContent, String fileName) {

        this.fileContent = fileContent;
        this.fileName = fileName;
    }

    public String getFileContent() {
        return fileContent;
    }
    public String getFileName() {
        return fileName;
    }
    @Override
    public String toString() {
        return "{\"fileContent\":\"" +fileContent + "\"," +
                "\"fileName\":\"" +fileName + '\"' +
                '}';
    }
}

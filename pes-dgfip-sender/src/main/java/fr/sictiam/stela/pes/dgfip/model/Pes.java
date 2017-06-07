package fr.sictiam.stela.pes.dgfip.model;

public class Pes {

    private String uuid;
    private String title;
    private String file;
    private String comment;

    public Pes() {
    }

    public Pes(String title, String file, String comment) {

        this.title = title;
        this.comment = comment;
        this.file = file;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "Pes{" +
                "uuid='" + uuid + '\'' +
                ", title='" + title + '\'' +
                ", file='" + file + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }
}

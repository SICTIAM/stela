package fr.sictiam.stela.pes.dgfip.model;

public class PesSend {

    private String pesId;
    private String dateSend;

    public PesSend() {

    }
    public PesSend(String pesId, String dateSend) {

        this.pesId = pesId;
        this.dateSend = dateSend;
    }
    public String getPesId() {
        return pesId;
    }
    public String getDateSend() {
        return dateSend;
    }
    @Override
    public String toString() {
        return "{\"pesId\":\"" + pesId + '\"' + "," +
                "\"dateSend\":\"" + dateSend + '\"'  +
                '}';
    }
}

package fr.sictiam.stela.pescommand.command;

import org.axonframework.commandhandling.TargetAggregateIdentifier;

import java.util.UUID;

public class SendPesCommand {

    @TargetAggregateIdentifier
    private String id;

    private String pesId;
    private String dateSend;

    public SendPesCommand() {
        this.id = UUID.randomUUID().toString();
    }

    public SendPesCommand(String id, String pesId, String dateSend) {
        this.id = id;
        this.pesId = pesId;
        this.dateSend = dateSend;
    }

    public String getId() {return id;}
    public String getPesId() {
        return pesId;
    }
    public String getDateSend() {
        return dateSend;
    }

}

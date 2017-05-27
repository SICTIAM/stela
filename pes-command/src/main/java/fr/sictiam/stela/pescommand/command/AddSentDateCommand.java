package fr.sictiam.stela.pescommand.command;

import org.axonframework.commandhandling.TargetAggregateIdentifier;

import java.util.Date;

public class AddSentDateCommand {

    @TargetAggregateIdentifier
    private String pesId;
    private Date sentDate;

    public AddSentDateCommand() {
    }

    public AddSentDateCommand(String pesId, Date sentDate) {
        this.pesId = pesId;
        this.sentDate = sentDate;
    }

    public String getPesId() {
        return pesId;
    }
    public Date getSentDate() {
        return sentDate;
    }
}

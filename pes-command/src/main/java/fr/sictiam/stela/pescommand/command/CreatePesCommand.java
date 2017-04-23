package fr.sictiam.stela.pescommand.command;

import org.axonframework.commandhandling.TargetAggregateIdentifier;

public class CreatePesCommand {

    @TargetAggregateIdentifier
    private String id;

    public CreatePesCommand(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}

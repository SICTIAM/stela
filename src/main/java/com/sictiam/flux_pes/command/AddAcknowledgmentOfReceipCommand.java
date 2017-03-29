package com.sictiam.flux_pes.command;

import org.axonframework.commandhandling.TargetAggregateIdentifier;

public class AddAcknowledgmentOfReceipCommand {

    @TargetAggregateIdentifier
    private String aggregateId;

    private String filename;

    public String getAggregateId() {
        return aggregateId;
    }

    public String getFilename() {
        return filename;
    }
}

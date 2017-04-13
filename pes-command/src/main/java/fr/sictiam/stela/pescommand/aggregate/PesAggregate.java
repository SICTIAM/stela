package fr.sictiam.stela.pescommand.aggregate;

import fr.sictiam.stela.pescommand.command.CreatePesCommand;
import fr.sictiam.stela.pescommand.event.PesCreatedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@Aggregate
public class PesAggregate {

    private static final Logger LOGGER = LoggerFactory.getLogger(PesAggregate.class);

    @AggregateIdentifier
    private String id;

    public PesAggregate() {}

    @CommandHandler
    public PesAggregate(CreatePesCommand createPesCommand) {
        LOGGER.debug("Received a command to create a PES ", createPesCommand.getId());
        apply(new PesCreatedEvent(createPesCommand.getId()));
    }
}

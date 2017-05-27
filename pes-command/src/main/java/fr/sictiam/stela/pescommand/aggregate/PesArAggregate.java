package fr.sictiam.stela.pescommand.aggregate;

import fr.sictiam.stela.pescommand.command.CreatePesArCommand;
import fr.sictiam.stela.pescommand.event.PesArCreatedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@Aggregate
public class PesArAggregate {

    private static final Logger LOGGER = LoggerFactory.getLogger(PesArAggregate.class);

    @AggregateIdentifier
    private String id;

    private String fileContent;
    private String fileName;


    private PesArAggregate() {}

    @CommandHandler
    public PesArAggregate(CreatePesArCommand createPesarCommand) {
        LOGGER.debug("Received a command to create a PES AR with id {}", createPesarCommand);
        apply(new PesArCreatedEvent(createPesarCommand.getId(),createPesarCommand.getFileContent(),createPesarCommand.getFileName()));
    }

    @EventSourcingHandler
    public void on(PesArCreatedEvent event) {
        this.id = event.getId();
        this.fileContent = event.getFileContent();
        this.fileName = event.getFileName();
    }
}

package fr.sictiam.stela.pescommand.aggregate;

import fr.sictiam.stela.pescommand.command.CreatePesCommand;
import fr.sictiam.stela.pescommand.event.PesCreatedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@Aggregate
public class PesAggregate {

    private static final Logger LOGGER = LoggerFactory.getLogger(PesAggregate.class);

    @AggregateIdentifier
    private String id;
    private String title;
    private String fileContent;
    private String fileName;
    private String comment;
    private Integer groupId;
    private Integer userId;

    private PesAggregate() {}

    @CommandHandler
    public PesAggregate(CreatePesCommand createPesCommand) {
        LOGGER.debug("Received a command to create a PES with id {}", createPesCommand);
        apply(new PesCreatedEvent(createPesCommand.getId(),createPesCommand.getTitle(),createPesCommand.getFileContent(),createPesCommand.getFileName(),createPesCommand.getComment(),createPesCommand.getGroupId(),createPesCommand.getUserId()));
    }

    @EventSourcingHandler
    public void on(PesCreatedEvent event) {
        this.id = event.getId();
        this.title = event.getTitle();
        this.fileContent = event.getFileContent();
        this.fileName = event.getFileName();
        this.comment = event.getComment();
        this.groupId = event.getGroupId();
        this.userId = event.getUserId();
    }
}

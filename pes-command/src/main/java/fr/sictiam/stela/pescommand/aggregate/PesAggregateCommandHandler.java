package fr.sictiam.stela.pescommand.aggregate;

import fr.sictiam.stela.pescommand.command.AddSentDateCommand;
import fr.sictiam.stela.pescommand.command.CreatePesCommand;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.Aggregate;
import org.axonframework.commandhandling.model.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PesAggregateCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PesAggregateCommandHandler.class);

    private final Repository<PesAggregate> repository;

    public PesAggregateCommandHandler(Repository<PesAggregate> repository) {
        this.repository = repository;
    }

    @CommandHandler
    public void handleCreateCompany(CreatePesCommand command) throws Exception {
        LOGGER.debug("Received a command to create a new PES with id {}", command.getId());
        repository.newInstance(() -> new PesAggregate(command.getId(),
                command.getTitle(),
                command.getFileContent(),
                command.getFileName(),
                command.getComment(),
                command.getGroupId(),
                command.getUserId()));
    }

    @CommandHandler
    public void addSentDate(AddSentDateCommand addSentDateCommand) {
        LOGGER.debug("Received a command to add sent date to PES with id {}", addSentDateCommand.getPesId());
        Aggregate<PesAggregate> pesAggregate = repository.load(addSentDateCommand.getPesId());
        pesAggregate.execute(aggregateRoot -> aggregateRoot.updateSentDate(addSentDateCommand.getSentDate()));
    }
}

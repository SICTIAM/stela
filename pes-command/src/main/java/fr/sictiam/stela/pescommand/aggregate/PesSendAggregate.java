package fr.sictiam.stela.pescommand.aggregate;

import fr.sictiam.stela.pescommand.command.SendPesCommand;
import fr.sictiam.stela.pescommand.event.PesSentEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@Aggregate
public class PesSendAggregate {

    private static final Logger LOGGER = LoggerFactory.getLogger(PesSendAggregate.class);

    @AggregateIdentifier
    private String id;

    private String pesId;
    private String dateSend;

    private PesSendAggregate() {}

    @CommandHandler
    public PesSendAggregate(SendPesCommand sendPesCommand) {
        LOGGER.debug("Received a command to know a PES sended {}", sendPesCommand);
        apply(new PesSentEvent(sendPesCommand.getId(),sendPesCommand.getPesId(),sendPesCommand.getDateSend()));
    }

    @EventSourcingHandler
    public void on(PesSentEvent event) {
        this.id = event.getId();
        this.pesId = event.getPesId();
        this.dateSend = event.getDateSend();

    }
}

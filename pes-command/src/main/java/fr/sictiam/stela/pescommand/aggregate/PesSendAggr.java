package fr.sictiam.stela.pescommand.aggregate;

import fr.sictiam.stela.pescommand.command.CreatePesCommand;
import fr.sictiam.stela.pescommand.command.SendPesCommand;
import fr.sictiam.stela.pescommand.event.PesCreatedEvent;
import fr.sictiam.stela.pescommand.event.PesSendedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@Aggregate
public class PesSendAggr {

    private static final Logger LOGGER = LoggerFactory.getLogger(PesSendAggr.class);

    @AggregateIdentifier
    private String id;

    private String pesId;
    private String dateSend;

    private PesSendAggr() {}

    @CommandHandler
    public PesSendAggr(SendPesCommand sendPesCommand) {
        LOGGER.debug("Received a command to know a PES sended {}", sendPesCommand);
        apply(new PesSendedEvent(sendPesCommand.getId(),sendPesCommand.getPesId(),sendPesCommand.getDateSend()));
    }

    @EventSourcingHandler
    public void on(PesSendedEvent event) {
        this.id = event.getId();
        this.pesId = event.getPesId();
        this.dateSend = event.getDateSend();

    }
}

package fr.sictiam.stela.pesarserv.aggregate;

import fr.sictiam.stela.pesarserv.command.ReceivePesAr;
import fr.sictiam.stela.pesarserv.event.ReceivePesArEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

/**
 * Created by s.vergon on 18/05/2017.
 */
public class ReceivePesArAggr {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReceivePesArAggr.class);

    @AggregateIdentifier
    private String id;
    private String fileContent;
    private String fileName;
    @Autowired
    private ReceivePesArAggr() {}

    @CommandHandler
    public ReceivePesArAggr(ReceivePesAr receivepesar) {
        LOGGER.debug("Received a command to create PES AR with id {}", receivepesar.getId());
        apply(new ReceivePesArEvent(receivepesar.getId(),receivepesar.getFileContent(),receivepesar.getFileName()));
    }

    @EventSourcingHandler
    public void on(ReceivePesArEvent event) {
        this.id = event.getId();
        this.fileContent = event.getFileContent();
        this.fileName = event.getFileName();
    }
}

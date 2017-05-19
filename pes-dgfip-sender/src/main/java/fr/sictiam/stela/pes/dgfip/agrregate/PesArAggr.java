package fr.sictiam.stela.pes.dgfip.agrregate;
import fr.sictiam.stela.pes.dgfip.command.CreatePesAr;
import fr.sictiam.stela.pes.dgfip.event.CreateEventPesAr;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;
/**
 * Created by s.vergon on 16/05/2017.
 */
@Aggregate
public class PesArAggr {

    private static final Logger LOGGER = LoggerFactory.getLogger(PesArAggr.class);

    @AggregateIdentifier
    private String id;
    private String fileContent;
    private String fileName;
    @Autowired
    private PesArAggr() {}

    @CommandHandler
    public PesArAggr(CreatePesAr createpesar) {
        LOGGER.debug("Received a command to transmit PES AR with id {}", createpesar.getId());
        apply(new CreateEventPesAr(createpesar.getId(),createpesar.getFileContent(),createpesar.getFileName()));
    }

    @EventSourcingHandler
    public void on(CreateEventPesAr event) {
        this.id = event.getId();
        this.fileContent = event.getFileContent();
        this.fileName = event.getFileName();
    }
}

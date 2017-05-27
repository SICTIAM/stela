package fr.sictiam.stela.pescommand.aggregate;

import fr.sictiam.stela.pescommand.event.PesCreatedEvent;
import fr.sictiam.stela.pescommand.event.PesSentEvent;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@Aggregate
public class PesAggregate {

    private static final Logger LOGGER = LoggerFactory.getLogger(PesAggregate.class);

    @AggregateIdentifier
    private String id;

    private PesAggregate() {}

    public PesAggregate(String id, String title, String fileContent, String fileName, String comment, Integer groupId, Integer userId) {
        apply(new PesCreatedEvent(id, title, fileContent, fileName, comment, groupId, userId));
    }

    @EventSourcingHandler
    public void on(PesCreatedEvent event) {
        LOGGER.debug("Received PES created event");
        this.id = event.getId();
    }

    public void updateSentDate(Date sentDate) {
        apply(new PesSentEvent(id, sentDate));
    }

    public String getId() {
        return id;
    }
}

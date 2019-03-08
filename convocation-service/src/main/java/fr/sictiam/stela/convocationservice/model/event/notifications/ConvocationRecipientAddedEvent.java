package fr.sictiam.stela.convocationservice.model.event.notifications;

import fr.sictiam.stela.convocationservice.model.Convocation;
import fr.sictiam.stela.convocationservice.model.Recipient;
import org.springframework.context.ApplicationEvent;

import java.util.Set;

public class ConvocationRecipientAddedEvent extends ApplicationEvent {

    private Convocation convocation;

    private Set<Recipient> recipients;

    public ConvocationRecipientAddedEvent(Object source, Convocation convocation, Set<Recipient> recipients) {
        super(source);
        this.convocation = convocation;
        this.recipients = recipients;
    }

    public Convocation getConvocation() {
        return convocation;
    }

    public Set<Recipient> getRecipients() {
        return recipients;
    }
}

package fr.sictiam.stela.convocationservice.model.event.notifications;

import fr.sictiam.stela.convocationservice.model.Convocation;
import fr.sictiam.stela.convocationservice.model.Recipient;
import org.springframework.context.ApplicationEvent;

public class ConvocationReadEvent extends ApplicationEvent {

    private Convocation convocation;

    private Recipient recipient;

    public ConvocationReadEvent(Object source, Convocation convocation, Recipient recipient) {
        super(source);
        this.convocation = convocation;
        this.recipient = recipient;
    }

    public Convocation getConvocation() {
        return convocation;
    }

    public Recipient getRecipient() {
        return recipient;
    }
}

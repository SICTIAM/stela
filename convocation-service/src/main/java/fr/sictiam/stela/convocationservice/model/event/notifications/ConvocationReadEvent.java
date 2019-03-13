package fr.sictiam.stela.convocationservice.model.event.notifications;

import fr.sictiam.stela.convocationservice.model.Convocation;
import fr.sictiam.stela.convocationservice.model.RecipientResponse;
import org.springframework.context.ApplicationEvent;

public class ConvocationReadEvent extends ApplicationEvent {

    private Convocation convocation;

    private RecipientResponse recipientResponse;

    public ConvocationReadEvent(Object source, Convocation convocation, RecipientResponse recipientResponse) {
        super(source);
        this.convocation = convocation;
        this.recipientResponse = recipientResponse;
    }

    public Convocation getConvocation() {
        return convocation;
    }

    public RecipientResponse getRecipientResponse() {
        return recipientResponse;
    }
}

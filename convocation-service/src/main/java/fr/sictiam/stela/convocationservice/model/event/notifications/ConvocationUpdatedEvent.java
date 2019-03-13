package fr.sictiam.stela.convocationservice.model.event.notifications;

import fr.sictiam.stela.convocationservice.model.Convocation;
import org.springframework.context.ApplicationEvent;

import java.util.List;

public class ConvocationUpdatedEvent extends ApplicationEvent {

    private Convocation convocation;

    private List<String> updates;

    public ConvocationUpdatedEvent(Object source, Convocation convocation, List<String> updates) {
        super(source);
        this.convocation = convocation;
        this.updates = updates;
    }

    public Convocation getConvocation() {
        return convocation;
    }

    public List<String> getUpdates() {
        return updates;
    }
}

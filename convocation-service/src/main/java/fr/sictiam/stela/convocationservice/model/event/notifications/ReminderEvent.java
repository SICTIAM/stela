package fr.sictiam.stela.convocationservice.model.event.notifications;

import fr.sictiam.stela.convocationservice.model.Convocation;
import org.springframework.context.ApplicationEvent;

public class ReminderEvent extends ApplicationEvent {

    private Convocation convocation;

    public ReminderEvent(Object source, Convocation convocation) {
        super(source);
        this.convocation = convocation;
    }

    public Convocation getConvocation() {
        return convocation;
    }
}

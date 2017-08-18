package fr.sictiam.stela.acteservice.model.event;

import fr.sictiam.stela.acteservice.model.ActeHistory;
import org.springframework.context.ApplicationEvent;

public class ActeHistoryEvent extends ApplicationEvent {

    private ActeHistory acteHistory;

    public ActeHistoryEvent(Object source, ActeHistory acteHistory) {
        super(source);
        this.acteHistory = acteHistory;
    }

    public ActeHistory getActeHistory() {
        return acteHistory;
    }
}

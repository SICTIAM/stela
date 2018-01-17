package fr.sictiam.stela.pesservice.model.event;

import org.springframework.context.ApplicationEvent;

import fr.sictiam.stela.pesservice.model.PesHistory;

public class PesHistoryEvent extends ApplicationEvent {

    private PesHistory pesHistory;

    public PesHistoryEvent(Object source, PesHistory pesHistory) {
        super(source);
        this.pesHistory = pesHistory;
    }

    public PesHistory getPesHistory() {
        return pesHistory;
    }
}

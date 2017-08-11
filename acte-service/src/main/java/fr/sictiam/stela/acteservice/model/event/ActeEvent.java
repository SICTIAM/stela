package fr.sictiam.stela.acteservice.model.event;

import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.model.StatusType;
import org.springframework.context.ApplicationEvent;

public class ActeEvent extends ApplicationEvent {

    private Acte acte;
    private StatusType status;

    public ActeEvent(Object source, Acte acte, StatusType status) {
        super(source);
        this.acte = acte;
        this.status = status;
    }

    public Acte getActe() {
        return acte;
    }

    public StatusType getStatus() {
        return status;
    }
}

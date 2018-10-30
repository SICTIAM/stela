package fr.sictiam.stela.pesservice.model.event;

import fr.sictiam.stela.pesservice.model.PesAller;
import org.springframework.context.ApplicationEvent;

public class PesCreationEvent extends ApplicationEvent {

    PesAller pesAller;

    public PesCreationEvent(Object source, PesAller pesAller) {
        super(source);
        this.pesAller = pesAller;
    }

    public PesAller getPesAller() {
        return pesAller;
    }
}

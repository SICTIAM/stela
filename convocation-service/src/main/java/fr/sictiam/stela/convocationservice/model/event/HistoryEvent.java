package fr.sictiam.stela.convocationservice.model.event;

import fr.sictiam.stela.convocationservice.model.Convocation;
import fr.sictiam.stela.convocationservice.model.HistoryType;
import org.springframework.context.ApplicationEvent;

public class HistoryEvent extends ApplicationEvent {

    private Convocation convocation;

    private HistoryType type;

    private String message;

    public HistoryEvent(Object source, Convocation convocation, HistoryType type) {
        this(source, convocation, type, null);
    }

    public HistoryEvent(Object source, Convocation convocation, HistoryType type, String message) {
        super(source);
        this.convocation = convocation;
        this.type = type;
        this.message = message;
    }

    public Convocation getConvocation() {
        return convocation;
    }

    public HistoryType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
}

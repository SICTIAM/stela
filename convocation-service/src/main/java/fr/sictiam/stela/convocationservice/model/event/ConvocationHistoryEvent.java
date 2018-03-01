package fr.sictiam.stela.convocationservice.model.event;

import fr.sictiam.stela.convocationservice.model.ConvocationHistory;
import org.springframework.context.ApplicationEvent;

public class ConvocationHistoryEvent extends ApplicationEvent {

    private ConvocationHistory convocationHistory;

    public ConvocationHistory getConvocationHistory() {
        return convocationHistory;
    }

    public ConvocationHistoryEvent(Object source, ConvocationHistory convocationHistory) {
        super(source);
        this.convocationHistory = convocationHistory;
    }

}

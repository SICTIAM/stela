package fr.sictiam.stela.convocationservice;

import fr.sictiam.stela.convocationservice.model.HistoryType;
import fr.sictiam.stela.convocationservice.model.event.ConvocationHistoryEvent;
import org.springframework.context.ApplicationListener;

class MockConvocationEventListener implements ApplicationListener<ConvocationHistoryEvent> {

    HistoryType status;

    public MockConvocationEventListener(HistoryType status) {
        this.status = status;
    }

    @Override
    public void onApplicationEvent(ConvocationHistoryEvent event) {
        if (event.getConvocationHistory().getType().equals(status)) {
            synchronized (this) {
                notifyAll();
            }
        }
    }

}
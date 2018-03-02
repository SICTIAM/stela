package fr.sictiam.stela.convocationservice;

import fr.sictiam.stela.convocationservice.model.StatusType;
import fr.sictiam.stela.convocationservice.model.event.ConvocationHistoryEvent;
import org.springframework.context.ApplicationListener;

class MockConvocationEventListener implements ApplicationListener<ConvocationHistoryEvent> {

    StatusType status;

    public MockConvocationEventListener(StatusType status) {
        this.status = status;
    }

    @Override
    public void onApplicationEvent(ConvocationHistoryEvent event) {
        if (event.getConvocationHistory().getStatus().equals(status)) {
            synchronized (this) {
                notifyAll();
            }
        }
    }

}
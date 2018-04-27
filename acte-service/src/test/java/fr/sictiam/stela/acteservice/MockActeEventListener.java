package fr.sictiam.stela.acteservice;

import fr.sictiam.stela.acteservice.model.StatusType;
import fr.sictiam.stela.acteservice.model.event.ActeHistoryEvent;
import org.springframework.context.ApplicationListener;

class MockActeEventListener implements ApplicationListener<ActeHistoryEvent> {

    StatusType status;

    public MockActeEventListener(StatusType status) {
        this.status = status;
    }

    @Override
    public void onApplicationEvent(ActeHistoryEvent event) {
        if (event.getActeHistory().getStatus().equals(status)) {
            synchronized (this) {
                notifyAll();
            }
        }
    }

}
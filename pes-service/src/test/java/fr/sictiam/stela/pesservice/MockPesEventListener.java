package fr.sictiam.stela.pesservice;

import org.springframework.context.ApplicationListener;

import fr.sictiam.stela.pesservice.model.StatusType;
import fr.sictiam.stela.pesservice.model.event.PesHistoryEvent;

class MockPesEventListener implements ApplicationListener<PesHistoryEvent> {

    StatusType status;

    public MockPesEventListener(StatusType status) {
        this.status = status;
    }

    @Override
    public void onApplicationEvent(PesHistoryEvent event) {
        if (event.getPesHistory().getStatus().equals(status)) {
            synchronized (this) {
                notifyAll();
            }
        }
    }

}
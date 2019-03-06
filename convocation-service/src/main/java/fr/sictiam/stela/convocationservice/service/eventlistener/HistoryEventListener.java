package fr.sictiam.stela.convocationservice.service.eventlistener;

import fr.sictiam.stela.convocationservice.dao.ConvocationHistoryRepository;
import fr.sictiam.stela.convocationservice.model.Convocation;
import fr.sictiam.stela.convocationservice.model.ConvocationHistory;
import fr.sictiam.stela.convocationservice.model.HistoryType;
import fr.sictiam.stela.convocationservice.model.event.HistoryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class HistoryEventListener {

    private final static Logger LOGGER = LoggerFactory.getLogger(HistoryEventListener.class);

    private final ConvocationHistoryRepository convocationHistoryRepository;

    @Autowired
    public HistoryEventListener(
            ConvocationHistoryRepository convocationHistoryRepository) {
        this.convocationHistoryRepository = convocationHistoryRepository;
    }


    @EventListener
    @Async
    public void addHistory(HistoryEvent event) {

        Convocation convocation = event.getConvocation();
        HistoryType type = event.getType();
        String message = event.getMessage();

        convocationHistoryRepository.saveAndFlush(new ConvocationHistory(convocation, type, message));
    }
}

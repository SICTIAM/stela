package fr.sictiam.stela.pesquery.event;

import fr.sictiam.stela.pescommand.event.PesCreatedEvent;
import fr.sictiam.stela.pesquery.dao.PesRepository;
import fr.sictiam.stela.pesquery.model.PesEntry;
import org.axonframework.eventhandling.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

@Component
public class PesEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PesEventListener.class);

    private PesRepository pesRepository;
    //private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    public PesEventListener(PesRepository pesRepository/*, SimpMessageSendingOperations messagingTemplate*/) {
        this.pesRepository = pesRepository;
        //this.messagingTemplate = messagingTemplate;
    }

    @EventHandler
    public void on(PesCreatedEvent event) {
        LOGGER.debug("Received a PES created event with id {}", event.getId());

        pesRepository.save(new PesEntry(event.getId()));

        //broadcastUpdates();
    }

/*
    private void broadcastUpdates() {
        Iterable<PesEntry> pesEntries = pesRepository.findAll();
        messagingTemplate.convertAndSend("/topic/pes.updates", pesEntries);
    }
*/
}

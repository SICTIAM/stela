package fr.sictiam.stela.pesarserv.event;

import fr.sictiam.stela.pescommand.event.PesCreatedEvent;
import fr.sictiam.stela.pesquery.dao.PesRepository;
import fr.sictiam.stela.pesquery.event.PesEventListener;
import fr.sictiam.stela.pesquery.model.PesEntry;
import org.axonframework.eventhandling.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by s.vergon on 18/05/2017.
 */
@Component
public class ListenerPesArEvent {

    private static final Logger LOGGER = LoggerFactory.getLogger(PesEventListener.class);

    private PesRepository pesRepository;

    @Autowired
    public ListenerPesArEvent(PesRepository pesRepository) {
        this.pesRepository = pesRepository;
    }

    @EventHandler
    public void on(ReceivePesArEvent event) {
        LOGGER.debug("Received a PES Ar Received event with id {} title {} fileContent {} comment {} groupid {} userid {}", event.getId(), event.getFileContent());

        //pesRepository.save(new PesEntry(event.getId(),event.getTitle(),event.getFileContent(),event.getFileName(),event.getComment(),event.getGroupId(),event.getUserId()));
    }
}

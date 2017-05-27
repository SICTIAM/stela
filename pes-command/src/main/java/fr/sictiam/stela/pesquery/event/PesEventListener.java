package fr.sictiam.stela.pesquery.event;

import fr.sictiam.stela.pescommand.event.PesCreatedEvent;
import fr.sictiam.stela.pescommand.event.PesSentEvent;
import fr.sictiam.stela.pesquery.dao.PesRepository;
import fr.sictiam.stela.pesquery.model.PesEntry;
import org.axonframework.eventhandling.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PesEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PesEventListener.class);

    private final PesRepository pesRepository;

    @Autowired
    public PesEventListener(PesRepository pesRepository) {
        this.pesRepository = pesRepository;
    }

    @EventHandler
    public void on(PesCreatedEvent event) {
        LOGGER.debug("Received a PES created event with id {} title {} fileContent {} comment {} groupid {} userid {}", event.getId(),event.getTitle(), event.getFileContent(),event.getComment(),event.getGroupId(),event.getUserId());

        pesRepository.save(new PesEntry(event.getId(),event.getTitle(),event.getFileContent(),event.getFileName(),event.getComment(),event.getGroupId(),event.getUserId()));
    }

    @EventHandler
    public void on(PesSentEvent event) {
        LOGGER.debug("Received a PES Sent event for PES id {} ", event.getPesId());
        PesEntry pesEntry = pesRepository.findOneByPesId(event.getPesId());
        pesEntry.setDateEnvoi(event.getSentDate());
        pesRepository.save(pesEntry);
    }
}

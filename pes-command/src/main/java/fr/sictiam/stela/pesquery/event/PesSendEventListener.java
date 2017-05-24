package fr.sictiam.stela.pesquery.event;

import fr.sictiam.stela.pescommand.event.PesCreatedEvent;
import fr.sictiam.stela.pescommand.event.PesSendedEvent;
import fr.sictiam.stela.pesquery.dao.PesRepository;
import fr.sictiam.stela.pesquery.model.PesEntry;
import org.axonframework.eventhandling.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PesSendEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PesSendEventListener.class);
    private PesRepository pesRepository;

    @Autowired
    public PesSendEventListener(PesRepository pesRepository) {
        this.pesRepository = pesRepository;
    }

    @EventHandler
    public void on(PesSendedEvent event) {
        LOGGER.debug("Received a PES Send event with pesid {} datesend {} ", event.getPesId(),event.getDateSend());
        //recherche du PES envoyé et mettre à jour la date
        PesEntry pesenvoye = pesRepository.findOneByPesId(event.getPesId());
        pesenvoye.setDateEnvoi(event.getDateSend());
        pesRepository.save(pesenvoye);
    }
}

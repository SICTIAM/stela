package fr.sictiam.stela.pesquery.event;

import fr.sictiam.stela.pescommand.event.PesArCreatedEvent;
import fr.sictiam.stela.pescommand.event.PesSendedEvent;
import org.axonframework.eventhandling.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PesArEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PesArEventListener.class);


    @Autowired
    public PesArEventListener() {

    }

    @EventHandler
    public void on(PesArCreatedEvent event) {
        LOGGER.debug("Received a PES AR event with id {} title {} fileContent {} comment {} groupid {} userid {}", event.getFileContent(),event.getFileName());
        //TODO : rechercher dans le fichier le nom de fichier pour pouvoir associer avec le PES créé et mettre le PES Entry en question à jour avec les infos de l'AR
    }
}

package fr.sictiam.stela.acteservice.service.init;

import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.model.ActeNature;
import fr.sictiam.stela.acteservice.service.ActeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;

@Component
@Profile("bootstrap-data")
public class DataInitializerService implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataInitializerService.class);

    private final ActeService acteService;

    @Autowired
    public DataInitializerService(ActeService acteService) {
        this.acteService = acteService;
    }

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        LOGGER.info("Bootstrap data asked");
        importData();
    }

    private void importData() {
        Acte acte001 = new Acte("001", LocalDate.now(), ActeNature.DELIBERATIONS, "1-0-0-1-0", "STELA 3 sera fini en Décembre", true);
        createDummyActe(acte001);

        Acte acte002 = new Acte("002", LocalDate.now(), ActeNature.DELIBERATIONS, "1-0-0-1-0", "SESILE 4 sera fini quand il sera fini", true);
        createDummyActe(acte002);

        Acte acte003 = new Acte("003", LocalDate.now(), ActeNature.DELIBERATIONS, "1-0-0-1-0", "Le DC Exporter sera mis en attente", true);
        createDummyActe(acte003);

        LOGGER.info("Bootstrapped some Actes");
    }

    private void createDummyActe(Acte acte) {
        try {
            acteService.create(acte, null, null);
        } catch (IOException e) {
            LOGGER.error("Unable to bootstrap acte {}", acte.getNumber());
        }
    }
}
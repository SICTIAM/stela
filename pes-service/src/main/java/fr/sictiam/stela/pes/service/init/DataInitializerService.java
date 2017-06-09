package fr.sictiam.stela.pes.service.init;

import fr.sictiam.stela.pes.model.Pes;
import fr.sictiam.stela.pes.service.PesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("clean-db")
public class DataInitializerService implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataInitializerService.class);

    private final PesService pesService;

    @Autowired
    public DataInitializerService(PesService pesService) {

        this.pesService = pesService;
    }

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        LOGGER.info("Data reset asked");
        importData();
    }

    private void importData() {

        Pes pesBudget = new Pes("Budget", "file", "Budget pour les véhicules");
        pesService.create(pesBudget);

        Pes pesSalaire = new Pes("Augmentation de salaire", "file", "Augmentation pour l'agent Jean-Michel Serieux");
        pesService.create(pesSalaire);

        Pes pesPrimes = new Pes("Primes de fin d'année", "file", "Montant des primes de fin d'année");
        pesService.create(pesPrimes);

        LOGGER.debug("Bootstrapped some PES");
    }
}

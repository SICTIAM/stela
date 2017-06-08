package fr.sictiam.stela.pes.service.init;

import fr.sictiam.stela.pes.dao.DaoService;
import fr.sictiam.stela.pes.model.Pes;
import fr.sictiam.stela.pes.service.PesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DataInitializerService implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataInitializerService.class);

    private final ApplicationArguments args;
    private final PesService pesService;
    private final DaoService daoService;

    @Autowired
    public DataInitializerService(ApplicationArguments args, PesService pesService, DaoService daoService) {

        this.args = args;
        this.pesService = pesService;
        this.daoService = daoService;
    }

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        if (args.containsOption("reset-db")) {
            LOGGER.info("Data reset asked");
            daoService.cleanDb();
            importData();
        }
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

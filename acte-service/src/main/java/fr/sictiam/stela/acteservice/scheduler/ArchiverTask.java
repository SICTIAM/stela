package fr.sictiam.stela.acteservice.scheduler;

import fr.sictiam.stela.acteservice.service.ArchiverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ArchiverTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiverTask.class);

    private final ArchiverService archiverService;

    public ArchiverTask(ArchiverService archiverService) {
        this.archiverService = archiverService;
    }

    @Scheduled(cron = "0 2 * * * *")
    public void archiveTask() {
        archiverService.archiveActesTask();
    }

    @Scheduled(cron = "0 1 * * * *")
    public void checkArchivesTask() {
        archiverService.checkArchivesStatusTask();
    }
}

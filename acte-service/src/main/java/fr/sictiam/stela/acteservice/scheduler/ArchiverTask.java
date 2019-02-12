package fr.sictiam.stela.acteservice.scheduler;

import fr.sictiam.stela.acteservice.service.ArchiverService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ArchiverTask {

    private final ArchiverService archiverService;

    public ArchiverTask(ArchiverService archiverService) {
        this.archiverService = archiverService;
    }

    @Scheduled(cron = "${application.archiverCrons.archiveTask}")
    public void archiveTask() {
        archiverService.archiveActesTask();
    }

    @Scheduled(cron = "${application.archiverCrons.checkArchivesTask}")
    public void checkArchivesTask() {
        archiverService.checkArchivesStatusTask();
    }
}

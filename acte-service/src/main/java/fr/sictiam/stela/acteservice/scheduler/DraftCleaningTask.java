package fr.sictiam.stela.acteservice.scheduler;

import fr.sictiam.stela.acteservice.model.Draft;
import fr.sictiam.stela.acteservice.service.DraftService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DraftCleaningTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(DraftCleaningTask.class);

    @Autowired
    private DraftService draftService;

    @Value("${application.drafts.maxDaysDraftLifetime}")
    private int maxDaysDraftLifetime;

    @Scheduled(cron = "${application.drafts.cleanCheckCron}")
    public void cleanDrafts() {
        LocalDateTime daysAgo = LocalDateTime.now().minusDays(maxDaysDraftLifetime);
        List<Draft> drafts = draftService.getAllLastModifiedBefore(daysAgo);
    	List<String> uuids = drafts.stream().map(Draft::getUuid).collect(Collectors.toList());
    	if(uuids.size() > 0) {
    	    LOGGER.debug("Cleaning {} old drafts.", uuids.size());
    	    draftService.deleteDrafts(uuids);
        }
    }
}

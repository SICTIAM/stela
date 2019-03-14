package fr.sictiam.stela.convocationservice.scheduler;

import fr.sictiam.stela.convocationservice.model.Convocation;
import fr.sictiam.stela.convocationservice.model.event.notifications.NoResponseInfoEvent;
import fr.sictiam.stela.convocationservice.service.ConvocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NoRecipientAnswerTask {

    private final static Logger LOGGER = LoggerFactory.getLogger(NoRecipientAnswerTask.class);

    private final ConvocationService convocationService;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public NoRecipientAnswerTask(
            ConvocationService convocationService,
            ApplicationEventPublisher applicationEventPublisher) {
        this.convocationService = convocationService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Scheduled(cron = "${application.convocation.cron.noResponseInformation}")
    public void reminderNotification() {

        LOGGER.info("Starting no answer information task");
        List<String> uuids = convocationService.getNoResponseInformation();

        for (String uuid : uuids) {
            Convocation convocation = convocationService.getConvocation(uuid);

            LOGGER.info("No response notification for convocation {} ({}) at {}", convocation.getSubject(),
                    convocation.getUuid(), convocation.getMeetingDate());
            applicationEventPublisher.publishEvent(new NoResponseInfoEvent(this, convocation));
        }
    }
}

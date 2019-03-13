package fr.sictiam.stela.convocationservice.scheduler;

import fr.sictiam.stela.convocationservice.model.Convocation;
import fr.sictiam.stela.convocationservice.model.event.notifications.ReminderEvent;
import fr.sictiam.stela.convocationservice.service.ConvocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReminderTask {

    private final static Logger LOGGER = LoggerFactory.getLogger(ReminderTask.class);

    private final ConvocationService convocationService;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Value("${application.convocation.reminderDelay}")
    private int reminderDelay;

    @Autowired
    public ReminderTask(
            ConvocationService convocationService,
            ApplicationEventPublisher applicationEventPublisher) {
        this.convocationService = convocationService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Scheduled(cron = "${application.convocation.cron.reminder}")
    public void reminderNotification() {

        LOGGER.info("Starting reminder task");
        List<Convocation> convocations = convocationService.getConvocationsToRemind(reminderDelay);

        for (Convocation convocation : convocations) {
            LOGGER.info("Reminder notification for convocation {} ({}) at {}", convocation.getSubject(),
                    convocation.getUuid(), convocation.getMeetingDate());
            applicationEventPublisher.publishEvent(new ReminderEvent(this, convocation));
        }
    }
}

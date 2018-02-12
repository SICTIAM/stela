package fr.sictiam.stela.acteservice.scheduler;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import fr.sictiam.stela.acteservice.model.PendingMessage;
import fr.sictiam.stela.acteservice.model.event.ActeHistoryEvent;
import fr.sictiam.stela.acteservice.service.ActeService;
import fr.sictiam.stela.acteservice.service.AdminService;
import fr.sictiam.stela.acteservice.service.PendingMessageService;

@Component
public class SenderTask implements ApplicationListener<ActeHistoryEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SenderTask.class);

    private Queue<PendingMessage> pendingQueue = new ConcurrentLinkedQueue<>();

    @Value("${application.archive.maxSizePerHour}")
    private Integer maxSizePerHour;

    private AtomicInteger currentSizeUsed = new AtomicInteger();

    @Autowired
    private ActeService acteService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private PendingMessageService pendingMessageService;

    @PostConstruct
    public void initQueue() {
        pendingQueue.addAll(pendingMessageService.getAllPendingMessages());
    }

    @Override
    public void onApplicationEvent(@NotNull ActeHistoryEvent event) {
        switch (event.getActeHistory().getStatus()) {
        case ARCHIVE_SIZE_CHECKED:
            pendingQueue.add(pendingMessageService.save(new PendingMessage(event.getActeHistory())));
            break;
        }
    }

    // reset limitation every hour
    @Scheduled(cron = "0 0 0/1 * * ?")
    public void resetLimitation() {
        currentSizeUsed.set(0);
    }

    @Scheduled(fixedRate = 100)
    public void senderTask() {

        if (!pendingQueue.isEmpty() && adminService.isMiatAvailable()) {
            PendingMessage pendingMessage = pendingQueue.peek();
            if ((pendingMessage.getFile().length + currentSizeUsed.get()) < maxSizePerHour) {

                HttpStatus sendStatus = null;
                try {
                    sendStatus = acteService.send(pendingMessage.getFile(), pendingMessage.getFileName());
                } catch (Exception e) {
                    sendStatus = HttpStatus.INTERNAL_SERVER_ERROR;
                }
                if (HttpStatus.OK.equals(sendStatus)) {
                    acteService.sent(pendingMessage.getActeUuid());
                    pendingMessageService.remove(pendingQueue.poll());
                    currentSizeUsed.addAndGet(pendingMessage.getFile().length);
                    LOGGER.info("Amount of data sent for this hour : " + currentSizeUsed);
                } else if (HttpStatus.NOT_FOUND.equals(sendStatus)) {
                    // pref offline
                    // just keep retrying
                } else if (HttpStatus.BAD_REQUEST.equals(sendStatus)
                        || HttpStatus.INTERNAL_SERVER_ERROR.equals(sendStatus)) {
                    // something wrong in what we send
                    // TODO when prefecture sending is "plugged", look if we can extract some useful
                    // info about the error
                    acteService.notSent(pendingMessage.getActeUuid());
                    pendingMessageService.remove(pendingQueue.poll());
                }
            } else {
                LOGGER.info("Hourly limit exceeded, waiting next hour");
            }
        }
    }

}

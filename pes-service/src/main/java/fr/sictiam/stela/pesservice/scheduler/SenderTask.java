package fr.sictiam.stela.pesservice.scheduler;

import fr.sictiam.stela.pesservice.dao.PendingMessageRepository;
import fr.sictiam.stela.pesservice.model.Attachment;
import fr.sictiam.stela.pesservice.model.PendingMessage;
import fr.sictiam.stela.pesservice.model.PesAller;
import fr.sictiam.stela.pesservice.model.StatusType;
import fr.sictiam.stela.pesservice.model.event.PesHistoryEvent;
import fr.sictiam.stela.pesservice.service.AdminService;
import fr.sictiam.stela.pesservice.service.PesAllerService;
import fr.sictiam.stela.pesservice.service.exceptions.PesSendException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SenderTask implements ApplicationListener<PesHistoryEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SenderTask.class);

    private Queue<PendingMessage> pendingQueue = new ConcurrentLinkedQueue<>();

    @Value("${application.archive.maxSizePerHour}")
    private Integer maxSizePerHour;

    private AtomicInteger currentSizeUsed = new AtomicInteger();

    @Autowired
    private PesAllerService pesService;

    @Autowired
    private PendingMessageRepository pendingMessageRepository;

    @Autowired
    private AdminService adminService;

    @PostConstruct
    public void initQueue() {
        pendingQueue.addAll(pendingMessageRepository.findAll());
    }

    @Override
    public void onApplicationEvent(@NotNull PesHistoryEvent event) {
        switch (event.getPesHistory().getStatus()) {
        case CREATED:
            pendingQueue.add(pendingMessageRepository.save(new PendingMessage(event.getPesHistory())));
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

        if (!pendingQueue.isEmpty() && adminService.isHeliosAvailable()) {
            PendingMessage pendingMessage = pendingQueue.peek();
            PesAller pes = pesService.getByUuid(pendingMessage.getPesUuid());
            Attachment attachment = pes.getAttachment();

            if ((attachment.getFile().length + currentSizeUsed.get()) < maxSizePerHour) {

                StatusType sendStatus = null;
                try {
                    pesService.send(pes);
                    sendStatus = StatusType.SENT;
                    pendingMessageRepository.delete(pendingQueue.poll());
                    currentSizeUsed.addAndGet(attachment.getFile().length);
                } catch (PesSendException e) {
                    LOGGER.error(e.getMessage());
                    sendStatus = StatusType.NOT_SENT;
                }
                pesService.updateStatus(pendingMessage.getPesUuid(), sendStatus, attachment.getFile(),
                        attachment.getFilename());

            } else {
                LOGGER.info("Hourly limit exceeded, waiting next hour");
            }
        }
    }

}

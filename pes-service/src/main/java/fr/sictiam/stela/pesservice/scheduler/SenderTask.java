package fr.sictiam.stela.pesservice.scheduler;

import fr.sictiam.stela.pesservice.dao.PendingMessageRepository;
import fr.sictiam.stela.pesservice.model.PendingMessage;
import fr.sictiam.stela.pesservice.model.PesAller;
import fr.sictiam.stela.pesservice.model.StatusType;
import fr.sictiam.stela.pesservice.model.event.PesHistoryEvent;
import fr.sictiam.stela.pesservice.service.AdminService;
import fr.sictiam.stela.pesservice.service.PesAllerService;
import fr.sictiam.stela.pesservice.service.StorageService;
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
import java.util.concurrent.atomic.AtomicLong;

@Component
public class SenderTask implements ApplicationListener<PesHistoryEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SenderTask.class);

    private Queue<PendingMessage> pendingQueue = new ConcurrentLinkedQueue<>();

    @Value("${application.archive.maxSizePerHour}")
    private Long maxSizePerHour;

    private AtomicLong currentSizeUsed = new AtomicLong();

    @Autowired
    private PesAllerService pesService;

    @Autowired
    private PendingMessageRepository pendingMessageRepository;

    @Autowired
    private AdminService adminService;

    @Autowired
    private StorageService storageService;

    @PostConstruct
    public void initQueue() {
        pendingQueue.addAll(pendingMessageRepository.findAll());
    }

    @Override
    public void onApplicationEvent(@NotNull PesHistoryEvent event) {
        if (StatusType.PENDING_SEND.equals(event.getPesHistory().getStatus())) {
            pendingQueue.add(pendingMessageRepository.save(new PendingMessage(event.getPesHistory())));
        }
    }

    // reset limitation every hour
    @Scheduled(cron = "0 0 0/1 * * ?")
    public void resetLimitation() {
        currentSizeUsed.set(0);
    }

    @Scheduled(fixedDelay = 5000)
    public void senderTask() {

        if (!pendingQueue.isEmpty() && adminService.isHeliosAvailable()) {
            PendingMessage pendingMessage = pendingQueue.peek();
            PesAller pes = pesService.getByUuid(pendingMessage.getPesUuid());

            if ((pes.getAttachment().getSize() + currentSizeUsed.get()) < maxSizePerHour) {

                LOGGER.debug("Sending PES {}: {}", pes.getUuid(), pes.getObjet());
                StatusType sendStatus;
                try {
                    pesService.send(pes);
                    sendStatus = StatusType.SENT;
                    pendingMessageRepository.delete(pendingQueue.poll());
                    currentSizeUsed.addAndGet(pes.getAttachment().getSize());
                } catch (PesSendException e) {
                    LOGGER.error("Error while sending PES : {}", e.getMessage());
                    sendStatus = StatusType.NOT_SENT;
                }
                byte[] content = storageService.getAttachmentContent(pes.getAttachment());
                pesService.updateStatus(pendingMessage.getPesUuid(), sendStatus, content,
                        pesService.renameFileToSend(pes));

            } else {
                LOGGER.info("Hourly limit exceeded, waiting next hour");
            }
        }
    }


}

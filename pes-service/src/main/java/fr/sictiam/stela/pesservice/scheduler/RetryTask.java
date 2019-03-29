package fr.sictiam.stela.pesservice.scheduler;

import fr.sictiam.stela.pesservice.model.PesAller;
import fr.sictiam.stela.pesservice.model.PesHistory;
import fr.sictiam.stela.pesservice.model.StatusType;
import fr.sictiam.stela.pesservice.service.PesAllerService;
import fr.sictiam.stela.pesservice.service.exceptions.PesSendException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class RetryTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetryTask.class);

    @Autowired
    PesAllerService pesAllerService;

    @Value("${application.retry.maxAttemps}")
    private Integer maxRetry;

    @Value("${application.retry.frequency}")
    private Integer frequency;

    @Scheduled(cron = "${application.retry.cron}")
    public void resendBlockedFlux() {
        LOGGER.info("Executing resendBlockedFlux task...");
        List<String> pesUuids = pesAllerService.getBlockedFlux();
        LOGGER.info("{} PES at waiting an ACK", pesUuids.size());
        pesUuids.forEach(uuid -> {

            PesHistory hist = pesAllerService.getLastSentHistory(uuid);

            if (LocalDateTime.now().isAfter(hist.getDate().plusHours(frequency))) {
                try {
                    LOGGER.info("Resending PES {}...", uuid);
                    PesAller pesAller = pesAllerService.getByUuid(uuid);
                    pesAllerService.send(pesAller);

                    StatusType statusType = StatusType.RESENT;
                    if (maxRetry <= (pesAller.getPesHistories().stream()
                            .filter(pesHistory -> pesHistory.getStatus().equals(StatusType.RESENT)).count() + 1)) {
                        statusType = StatusType.MAX_RETRY_REACH;
                    }
                    pesAllerService.updateStatus(uuid, statusType);
                } catch (PesSendException e) {
                    pesAllerService.updateStatus(uuid, StatusType.FILE_ERROR, e.getClass().getName());
                    LOGGER.error("Error while trying to resend PES: {}", uuid, e);
                }
            }

        });
    }

}

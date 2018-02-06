package fr.sictiam.stela.pesservice.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import fr.sictiam.stela.pesservice.model.PesAller;
import fr.sictiam.stela.pesservice.model.PesHistory;
import fr.sictiam.stela.pesservice.model.StatusType;
import fr.sictiam.stela.pesservice.service.PesAllerService;
import fr.sictiam.stela.pesservice.service.exceptions.PesSendException;

@Component
public class RetryTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetryTask.class);

    @Autowired
    PesAllerService pesAllerService;

    @Value("${application.retry.maxAttemps}")
    private Integer maxRetry;

    @Value("${application.retry.frequency}")
    private Integer frequency;

    @Scheduled(cron = "0 0 0/1 * * ?")
    public void resendBlockedFlux() {
        List<PesAller> pesList = pesAllerService.getBlockedFlux();
        pesList.forEach(pes -> {

            PesHistory hist = pesAllerService.getLastSentHistory(pes.getUuid());

            if (LocalDateTime.now().isAfter(hist.getDate().plusHours(frequency))) {
                try {
                    pesAllerService.send(pes);

                    StatusType statusType = StatusType.RESENT;
                    if (maxRetry <= (pes.getPesHistories().stream()
                            .filter(pesHistory -> pesHistory.getStatus().equals(StatusType.RESENT)).count() + 1)) {
                        statusType = StatusType.MAX_RETRY_REACH;
                    }
                    pesAllerService.updateStatus(pes.getUuid(), statusType);
                } catch (PesSendException e) {
                    pesAllerService.updateStatus(pes.getUuid(), StatusType.FILE_ERROR, e.getClass().getName());
                    LOGGER.error(e.getMessage());
                }
            }

        });
    }

}

package fr.sictiam.stela.pesservice.scheduler;


import fr.sictiam.stela.pesservice.model.StatusType;
import fr.sictiam.stela.pesservice.model.util.NotificationAttachement;
import fr.sictiam.stela.pesservice.service.ExternalRestService;
import fr.sictiam.stela.pesservice.service.LocalesService;
import fr.sictiam.stela.pesservice.service.PesAllerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class AnomalyTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnomalyTask.class);

    private final PesAllerService pesAllerService;

    private final ExternalRestService externalRestService;

    private final LocalesService localesService;

    @Autowired
    public AnomalyTask(PesAllerService pesAllerService, ExternalRestService externalRestService, LocalesService localesService) {
        this.pesAllerService = pesAllerService;
        this.externalRestService = externalRestService;
        this.localesService = localesService;
    }

    @Value("${application.anomaly.fixedDelay}")
    private Integer fixedDelay;

    @Value("${application.anomaly.rate}")
    private Integer rate;

    @Value("${application.anomaly.notificationLink}")
    private String notificationLink;

    @Scheduled(fixedDelayString = "${application.anomaly.fixedDelay}")
    public void checkAnomalyInPesAller() {
        LOGGER.debug("[checkAnomalyInPesAller] Running ...");
        List<NotificationAttachement.Field> fields = new ArrayList<>();

        LocalDateTime lastSchedulerTime;
        lastSchedulerTime = LocalDateTime.now().minus(fixedDelay + 1, ChronoField.MILLI_OF_DAY.getBaseUnit());
        LOGGER.debug("[checkAnomalyInPesAller] Count anomaly number of PesAller after last anomaly check time {}", lastSchedulerTime);

        Arrays.stream(StatusType.values()).filter(StatusType::isAnomaly).forEach(
            statusType -> {
                Long count = pesAllerService.countPesAllerByStatusTypeAndDate(statusType, lastSchedulerTime);
                LOGGER.info("[checkAnomalyInPesAller] PesAller with {} type, have {} anomalies", statusType.name(), count);
                if(count >= rate) {
                    LOGGER.debug("[checkAnomalyInPesAller] Number of PesAller anomaly is to", rate);
                    NotificationAttachement.Field field = new NotificationAttachement.Field();
                    field.setTitle(localesService.getMessage("fr", "pes", "$.pes.status." + statusType.name()));
                    field.setValue(count.toString());
                    fields.add(field);
                }
            }
        );

        if(fields.size() > 0)
            externalRestService.notifyAnomalyByMattermost(this.buildNotificationAttachments(fields), notificationLink);
    }

    private List<NotificationAttachement>  buildNotificationAttachments(List<NotificationAttachement.Field> fields) {
        List<NotificationAttachement> attachements = new ArrayList<>();


        NotificationAttachement attachement = new NotificationAttachement();
        attachement.setTitle(localesService.getMessage("fr", "pes", "$.pes.help_text.potential_anomamlies_detected"));
        attachement.setColor("#e80d0d");
        attachement.setFallback(localesService.getMessage("fr", "pes_notification", "$.pes.ANOMALIES.subject"));
        attachement.setFields(fields);

        attachements.add(attachement);

        return attachements;
    }
}

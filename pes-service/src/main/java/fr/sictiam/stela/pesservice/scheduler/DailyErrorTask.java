package fr.sictiam.stela.pesservice.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import fr.sictiam.stela.pesservice.model.Notification;
import fr.sictiam.stela.pesservice.model.PesAller;
import fr.sictiam.stela.pesservice.model.StatusType;
import fr.sictiam.stela.pesservice.service.ExternalRestService;
import fr.sictiam.stela.pesservice.service.LocalesService;
import fr.sictiam.stela.pesservice.service.NotificationService;
import fr.sictiam.stela.pesservice.service.PesAllerService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DailyErrorTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyErrorTask.class);

    @Autowired
    PesAllerService pesAllerService;

    @Autowired
    ExternalRestService externalRestService;

    @Autowired
    NotificationService notificationService;

    @Autowired
    TemplateEngine template;

    @Autowired
    LocalesService localesService;

    @Value("${application.url}")
    private String applicationUrl;

    @Value("${application.dailymail.active}")
    private boolean active;

    @Scheduled(cron = "${application.dailymail.cron}")
    public void sendDailyMail() {

        if (!active)
            return;

        LOGGER.info("Executing sendDailyMail task...");
        List<PesAller> pesList = pesAllerService.findAllByLastHistoryStatus(StatusType.NACK_RECEIVED);
        Map<String, Pair<List<String>, List<PesAller>>> localAuthorities = new HashMap<>();

        pesList.stream().forEach(pes -> {
            try {
                JsonNode profiles = externalRestService.getProfiles(pes.getLocalAuthority().getUuid());

                profiles.forEach(
                        profile -> {
                            String localAuthority = profile.get("localAuthority").get("slugName").asText();
                            if (!localAuthorities.containsKey(localAuthority)) {
                                localAuthorities.put(localAuthority, Pair.of(new ArrayList<>(), new ArrayList<>()));
                            }

                            if (hasNotifification(Notification.Type.DAILY_ERRORS, profile))
                                localAuthorities.get(localAuthority).getFirst().add(notificationService.getAgentMail(profile));

                            if (!localAuthorities.get(localAuthority).getSecond().contains(pes))
                                localAuthorities.get(localAuthority).getSecond().add(pes);
                        }
                );
            }
            catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        });

        localAuthorities.forEach((localAuthority, data) -> {
            Context ctx = new Context();
            ctx.setVariable("localAuthority", localAuthority);
            ctx.setVariable("pesList", data.getSecond());
            ctx.setVariable("baseUrl", applicationUrl);
            String msg = template.process("mails/dailyErrors_fr", ctx);

            try {
                notificationService.sendMailBcc(
                        data.getFirst().toArray(new String[data.getFirst().size()]),
                        localesService.getMessage("fr", "pes_notification","$.pes." + Notification.Type.DAILY_ERRORS.toString() + ".subject"),
                        msg
                );
            }
            catch (MessagingException | IOException e) {
                LOGGER.warn("Failed to send email : {}", e.getMessage());
            }
        });


    }

    private boolean hasNotifification (Notification.Type notification, JsonNode node) {

        for (int i = 0 ; i < node.get("notificationValues").size() ; i++) {
            JsonNode n = node.get("notificationValues").get(i);
            if (StringUtils.removeStart(n.get("name").asText(), "PES_").equals(notification.toString()) && n.get("active").asBoolean())
                return true;
        }
        return false;
    }

}

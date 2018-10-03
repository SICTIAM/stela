package fr.sictiam.stela.pesservice.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import fr.sictiam.stela.pesservice.model.LocalAuthority;
import fr.sictiam.stela.pesservice.model.Notification;
import fr.sictiam.stela.pesservice.model.PesAller;
import fr.sictiam.stela.pesservice.service.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(name = "application.dailymail.active", havingValue = "true")
public class DailyErrorTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyErrorTask.class);

    @Autowired
    PesAllerService pesAllerService;

    @Autowired
    ExternalRestService externalRestService;

    @Autowired
    NotificationService notificationService;

    @Autowired
    LocalAuthorityService localAuthorityService;

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

        LOGGER.info("Executing sendDailyMail task...");


        List<LocalAuthority> localAuthorities = localAuthorityService.getAll();
        localAuthorities.forEach(localAuthority -> {

            String uuid = localAuthority.getUuid();
            List<String> mails = new ArrayList<>();
            try {
                JsonNode profiles = externalRestService.getProfiles(uuid);
                profiles.forEach(profile -> {
                    if (hasNotifification(Notification.Type.DAILY_ERRORS, profile))
                        mails.add(notificationService.getAgentMail(profile));
                });
            } catch (IOException e) {
                LOGGER.warn("Failed to send email : {}", e.getMessage());
            }

            List<PesAller> pesList = pesAllerService.getPesInError(uuid);

            if (mails.size() > 0 && pesList.size() > 0) {
                Context ctx = new Context();
                ctx.setVariable("localAuthority", localAuthority.getSlugName());
                ctx.setVariable("pesList", pesList);
                ctx.setVariable("baseUrl", applicationUrl);
                String msg = template.process("mails/DAILY_ERRORS_fr", ctx);

                try {
                    notificationService.sendMailBcc(
                            mails.toArray(new String[mails.size()]),
                            localesService.getMessage("fr", "pes_notification", "$.pes." + Notification.Type.DAILY_ERRORS.toString() + ".subject"),
                            msg
                    );
                } catch (MessagingException | IOException e) {
                    LOGGER.warn("Failed to send email : {}", e.getMessage());
                }
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

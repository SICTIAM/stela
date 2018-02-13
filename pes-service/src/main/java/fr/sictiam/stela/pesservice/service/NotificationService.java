package fr.sictiam.stela.pesservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import fr.sictiam.stela.pesservice.model.Notification;
import fr.sictiam.stela.pesservice.model.NotificationValue;
import fr.sictiam.stela.pesservice.model.PesAller;
import fr.sictiam.stela.pesservice.model.PesHistory;
import fr.sictiam.stela.pesservice.model.StatusType;
import fr.sictiam.stela.pesservice.model.event.PesHistoryEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.validation.constraints.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class NotificationService implements ApplicationListener<PesHistoryEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    LocalesService localesService;

    @Autowired
    PesAllerService pesService;

    @Autowired
    ExternalRestService externalRestService;

    @Autowired
    JavaMailSender emailSender;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void onApplicationEvent(@NotNull PesHistoryEvent event) {
        List<StatusType> notificationTypes = Notification.notifications.stream().map(Notification::getStatusType)
                .collect(Collectors.toList());
        if (notificationTypes.contains(event.getPesHistory().getStatus())) {
            try {
                proccessEvent(event);
            } catch (MessagingException e) {
                LOGGER.error(e.getMessage());
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }

    public void proccessEvent(PesHistoryEvent event) throws MessagingException, IOException {
        PesAller pes = pesService.getByUuid(event.getPesHistory().getPesUuid());
        JsonNode node = externalRestService.getProfile(pes.getProfileUuid());

        Notification notification = Notification.notifications.stream()
                .filter(notif -> notif.getStatusType().equals(event.getPesHistory().getStatus())).findFirst().get();

        JsonNode profiles = externalRestService.getProfiles(pes.getLocalAuthority().getUuid());

        profiles.forEach(profile -> {
            if (StreamSupport.stream(profile.get("localAuthorityNotifications").spliterator(), false)
                    .anyMatch(value -> value.asText().equals("PES"))
                    && !pes.getProfileUuid().equals(profile.get("uuid").asText())) {
                List<NotificationValue> profileNotifications = getNotificationValues(profile);

                if (notification.isDeactivatable()
                        || profileNotifications.stream()
                                .anyMatch(notif -> notif.getName().equals(event.getPesHistory().getStatus().toString())
                                        && notif.isActive())
                        || (notification.isDefaultValue() && profileNotifications.isEmpty())) {
                    try {
                        sendMail(getAgentMail(profile),
                                localesService.getMessage("fr", "pes_notification",
                                        "$.pes.copy." + event.getPesHistory().getStatus().name() + ".subject"),
                                localesService.getMessage("fr", "pes_notification",
                                        "$.pes.copy." + event.getPesHistory().getStatus().name() + ".body",
                                        getAgentInfo(profile)));
                    } catch (MessagingException | IOException e) {
                        LOGGER.error(e.getMessage());
                    }
                }
            }
        });

        List<NotificationValue> notifications = getNotificationValues(node);

        if (notification.isDeactivatable() || notifications.stream().anyMatch(
                notif -> notif.getName().equals(event.getPesHistory().getStatus().toString()) && notif.isActive())
                || (notification.isDefaultValue() && notifications.isEmpty())) {
            sendMail(getAgentMail(node),
                    localesService.getMessage("fr", "pes_notification",
                            "$.pes." + event.getPesHistory().getStatus().name() + ".subject"),
                    localesService.getMessage("fr", "pes_notification",
                            "$.pes." + event.getPesHistory().getStatus().name() + ".body", getAgentInfo(node)));

            PesHistory pesHistory = new PesHistory(pes.getUuid(), StatusType.NOTIFICATION_SENT);
            applicationEventPublisher.publishEvent(new PesHistoryEvent(this, pesHistory));
        }
    }

    public List<NotificationValue> getNotificationValues(JsonNode node) {
        List<NotificationValue> notifications = new ArrayList<>();
        node.get("notificationValues").forEach(notif -> {
            if (StringUtils.startsWith(notif.get("active").asText(), "PES_"))
                notifications.add(new NotificationValue(notif.get("uuid").asText(),
                        StringUtils.removeStart(notif.get("name").asText(), "PES_"), notif.get("active").asBoolean()));
        });
        return notifications;
    }

    public void sendMail(String mail, String subject, String text) throws MessagingException, IOException {

        MimeMessage message = emailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setSubject(subject);
        helper.setText(text, true);
        helper.setTo(mail);

        emailSender.send(message);
    }

    public String getAgentMail(JsonNode node) {
        return StringUtils.isNotBlank(node.get("email").asText()) ? node.get("email").asText()
                : node.get("agent").get("email").asText();
    }

    public Map<String, String> getAgentInfo(JsonNode node) {
        Map<String, String> variables = new HashMap<>();
        variables.put("firstname", node.get("agent").get("given_name").asText());
        variables.put("lastname", node.get("agent").get("family_name").asText());
        return variables;
    }
}
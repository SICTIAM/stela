package fr.sictiam.stela.acteservice.service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sictiam.stela.acteservice.model.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;

import fr.sictiam.stela.acteservice.model.event.ActeHistoryEvent;

@Service
public class NotificationService implements ApplicationListener<ActeHistoryEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    LocalesService localesService;

    @Autowired
    ActeService acteService;

    @Autowired
    ExternalRestService externalRestService;

    @Autowired
    JavaMailSender emailSender;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void onApplicationEvent(@NotNull ActeHistoryEvent event) {
        List<StatusType> notificationTypes = Notification.notifications.stream()
                .map(Notification::getStatusType)
                .collect(Collectors.toList());
        if(notificationTypes.contains(event.getActeHistory().getStatus())) {
            try {
                proccessEvent(event);
            } catch (MessagingException e) {
                LOGGER.error(e.getMessage());
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }

    public void proccessEvent(ActeHistoryEvent event) throws MessagingException, IOException {
        Acte acte = acteService.getByUuid(event.getActeHistory().getActeUuid());
        JsonNode node = externalRestService.getProfile(acte.getProfileUuid());

        Notification notification = Notification.notifications.stream()
                .filter(notif -> notif.getStatusType().equals(event.getActeHistory().getStatus()))
                .findFirst().get();

        List<NotificationValue> notifications = new ArrayList<>();
        node.get("notificationValues").forEach(notif -> {
            if(StringUtils.startsWith(notif.get("active").asText(), "ACTE_"))
                notifications.add(new NotificationValue(
                        notif.get("uuid").asText(),
                        StringUtils.removeStart(notif.get("name").asText(), "ACTE_"),
                        notif.get("active").asBoolean()
                ));
        });
        if(notification.isDeactivatable()
            || notifications.stream().anyMatch(notif -> notif.getName().equals(event.getActeHistory().getStatus().toString()) && notif.isActive())
            || (notification.isDefaultValue() && notifications.isEmpty()))
            sendMail(acte.getUuid(), node, event.getActeHistory().getStatus());
    }

    private void sendMail(String uuid, JsonNode node, StatusType statusType) throws MessagingException {

        String mail = StringUtils.isNotBlank(node.get("email").asText()) ? node.get("email").asText() : node.get("agent").get("email").asText();
        String firstName = node.get("agent").get("given_name").asText();
        String lastName = node.get("agent").get("family_name").asText();

        Map<String, String> variables = new HashMap<>();
        variables.put("firstname", firstName);
        variables.put("lastname", lastName);

        String subject = localesService.getMessage("fr", "acte_notification",
                "$.acte." + statusType.name() + ".subject");
        String text = localesService.getMessage("fr", "acte_notification", "$.acte." + statusType.name() + ".body",
                variables);

        MimeMessage message = emailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setSubject(subject);
        helper.setText(text, true);
        helper.setTo(mail);

        emailSender.send(message);

        ActeHistory acteHistory = new ActeHistory(uuid, StatusType.NOTIFICATION_SENT);
        applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory));
    }

}

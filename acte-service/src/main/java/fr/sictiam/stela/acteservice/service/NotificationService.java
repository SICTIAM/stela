package fr.sictiam.stela.acteservice.service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.validation.constraints.NotNull;

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

import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.model.ActeHistory;
import fr.sictiam.stela.acteservice.model.StatusType;
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
        // TODO add every event that request mail notification
        switch (event.getActeHistory().getStatus()) {
        case ACK_RECEIVED:
        case SENT:
        case CANCELLED:
        case NACK_RECEIVED: {
            try {
                proccessEvent(event);
            } catch (MessagingException e) {
                LOGGER.error(e.getMessage());
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        }

        default:
            break;

        }
    }

    public void proccessEvent(ActeHistoryEvent event) throws MessagingException, IOException {
        Acte acte = acteService.getByUuid(event.getActeHistory().getActeUuid());
        JsonNode node = externalRestService.getProfile(acte.getProfileUuid());

        List<String> notifications = new ArrayList<>();
        node.get("notifications").forEach(notification -> notifications.add(notification.asText()));
        List<String> filteredNotifications = notifications.stream()
                .filter(notification -> StringUtils.startsWith(notification, "ACTE_"))
                .map(notification -> StringUtils.removeStart(notification, "ACTE_"))
                .collect(Collectors.toList());
        if(filteredNotifications.contains(event.getActeHistory().getStatus().toString()))
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

        // JavaMailSender emailSender = getJavaMailSender();
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

package fr.sictiam.stela.pesservice.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import fr.sictiam.stela.pesservice.model.Notification;
import fr.sictiam.stela.pesservice.model.NotificationValue;
import fr.sictiam.stela.pesservice.model.PesAller;
import fr.sictiam.stela.pesservice.model.PesHistory;
import fr.sictiam.stela.pesservice.model.StatusType;
import fr.sictiam.stela.pesservice.model.event.PesHistoryEvent;

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
        List<StatusType> notificationTypes = Notification.notifications.stream()
                .map(Notification::getStatusType)
                .collect(Collectors.toList());
        if(notificationTypes.contains(event.getPesHistory().getStatus())) {
            try {
                proccessEvent(event);
            } catch (MessagingException e) {
                LOGGER.error(e.getMessage());
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }
    
    public void proccessEvent(PesHistoryEvent event ) throws MessagingException, IOException {
        PesAller acte = pesService.getByUuid(event.getPesHistory().getPesUuid());
        JsonNode node = externalRestService.getProfile(acte.getProfileUuid());

        Notification notification = Notification.notifications.stream()
                .filter(notif -> notif.getStatusType().equals(event.getPesHistory().getStatus()))
                .findFirst().get();

        List<NotificationValue> notifications = new ArrayList<>();
        node.get("notificationValues").forEach(notif -> {
            if(StringUtils.startsWith(notif.get("active").asText(), "PES_"))
                notifications.add(new NotificationValue(
                        notif.get("uuid").asText(),
                        StringUtils.removeStart(notif.get("name").asText(), "PES_"),
                        notif.get("active").asBoolean()
                ));
        });
        if(notification.isDeactivatable()
            || notifications.stream().anyMatch(notif -> notif.getName().equals(event.getPesHistory().getStatus().toString()) && notif.isActive())
            || (notification.isDefaultValue() && notifications.isEmpty()))
            sendMail(acte.getUuid(), node, event.getPesHistory().getStatus());
    }

    public void sendMail(String uuid, JsonNode node, StatusType statusType) throws MessagingException, IOException {

        String mail = StringUtils.isNotBlank(node.get("email").asText()) ? node.get("email").asText() : node.get("agent").get("email").asText();
        String firstName = node.get("agent").get("given_name").asText();
        String lastName = node.get("agent").get("family_name").asText();

        Map<String, String> variables = new HashMap<>();
        variables.put("firstname", firstName);
        variables.put("lastname", lastName);

        String subject = localesService.getMessage("fr", "pes_notification",
                "$.pes." + statusType.name() + ".subject");
        String text = localesService.getMessage("fr", "pes_notification", "$.pes." + statusType.name() + ".body",
                variables);

        MimeMessage message = emailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setSubject(subject);
        helper.setText(text, true);
        helper.setTo(mail);

        emailSender.send(message);

        PesHistory pesHistory = new PesHistory(uuid, StatusType.NOTIFICATION_SENT);
        applicationEventPublisher.publishEvent(new PesHistoryEvent(this, pesHistory));
    }

}

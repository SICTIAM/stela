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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.validation.constraints.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
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
    TemplateEngine template;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Value("${application.url}")
    private String applicationUrl;

    @Override
    public void onApplicationEvent(@NotNull PesHistoryEvent event) {
        List<String> notificationTypes = Notification.notifications.stream().map(n -> n.getType().toString())
                .collect(Collectors.toList());
        if (notificationTypes.contains(event.getPesHistory().getStatus().toString())) {
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

        Notification notification = Notification.notifications.stream().filter(
                n -> n.getType().toString().equals(event.getPesHistory().getStatus().toString())
        ).findFirst().get();

        JsonNode profiles = externalRestService.getProfiles(pes.getLocalAuthority().getUuid());

        profiles.forEach(profile -> {
            if (StreamSupport.stream(profile.get("localAuthorityNotifications").spliterator(), false)
                    .anyMatch(value -> value.asText().equals("PES"))
                    && !pes.getProfileUuid().equals(profile.get("uuid").asText())) {
                List<NotificationValue> profileNotifications = getNotificationValues(profile);

                if (!notification.isDeactivatable() ||
                        profileNotifications.stream()
                                .anyMatch(notif -> notif.getName().equals(event.getPesHistory().getStatus().toString())
                                        && notif.isActive())
                        || (notification.isDefaultValue() && profileNotifications.isEmpty())) {
                    try {
                        Context ctx = new Context(Locale.FRENCH, getAgentInfo(profile));
                        ctx.setVariable("baseUrl", applicationUrl);
                        String msg = template.process("mails/copy_" + event.getPesHistory().getStatus().name() + "_fr", ctx);
                        sendMail(getAgentMail(profile),
                                localesService.getMessage("fr", "pes_notification",
                                        "$.pes.copy." + event.getPesHistory().getStatus().name() + ".subject"),
                                msg);
                    } catch (MessagingException | IOException e) {
                        LOGGER.error(e.getMessage());
                    }
                }
            }
        });

        if (StringUtils.isNotBlank(pes.getProfileUuid())) {
            JsonNode node = externalRestService.getProfile(pes.getProfileUuid());
            List<NotificationValue> notifications = getNotificationValues(node);

            if (!notification.isDeactivatable() ||
                    notifications.stream()
                            .anyMatch(notif -> notif.getName().equals(event.getPesHistory().getStatus().toString())
                                    && notif.isActive())
                    || (notification.isDefaultValue() && notifications.isEmpty())) {

                Context ctx = new Context(Locale.FRENCH, getAgentInfo(node));
                ctx.setVariable("pes", pes);
                ctx.setVariable("baseUrl", applicationUrl);
                ctx.setVariable("localAuthority", pes.getLocalAuthority().getSlugName());
                String msg = template.process("mails/" + event.getPesHistory().getStatus().name() + "_fr", ctx);
                sendMail(getAgentMail(node),
                        localesService.getMessage("fr", "pes_notification",
                                "$.pes." + event.getPesHistory().getStatus().name() + ".subject"),
                        msg);

                if (notification.isNotificationStatus()) {
                    PesHistory pesHistory = new PesHistory(pes.getUuid(), StatusType.NOTIFICATION_SENT);
                    pesService.updateHistory(pesHistory);
                    applicationEventPublisher.publishEvent(new PesHistoryEvent(this, pesHistory));
                }
            }
        }

    }

    public List<NotificationValue> getNotificationValues(JsonNode node) {
        List<NotificationValue> notifications = new ArrayList<>();
        node.get("notificationValues").forEach(notif -> {
            if (StringUtils.startsWith(notif.get("name").asText(), "PES_"))
                notifications.add(new NotificationValue(notif.get("uuid").asText(),
                        StringUtils.removeStart(notif.get("name").asText(), "PES_"), notif.get("active").asBoolean()));
        });
        return notifications;
    }

    public void sendMail(String mail, String subject, String text) throws MessagingException, IOException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setSubject(subject);
        helper.setText(text, true);
        helper.setTo(mail);

        emailSender.send(message);
    }

    public void sendMail(String[] mails, String subject, String text) throws MessagingException, IOException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setSubject(subject);
        helper.setText(text, true);
        helper.setTo(mails);

        emailSender.send(message);
    }

    public void sendMailBcc(String[] mails, String subject, String text) throws MessagingException, IOException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setSubject(subject);
        helper.setText(text, true);
        helper.setBcc(mails);

        emailSender.send(message);
    }

    public String getAgentMail(JsonNode node) {
        return !node.get("email").isNull() && StringUtils.isNotBlank(node.get("email").asText())
                ? node.get("email").asText()
                : node.get("agent").get("email").asText();
    }

    public Map<String, Object> getAgentInfo(JsonNode node) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("firstname", node.get("agent").get("given_name").asText());
        variables.put("lastname", node.get("agent").get("family_name").asText());
        return variables;
    }
}
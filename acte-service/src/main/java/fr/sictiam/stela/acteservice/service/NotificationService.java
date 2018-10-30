package fr.sictiam.stela.acteservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.model.ActeHistory;
import fr.sictiam.stela.acteservice.model.Notification;
import fr.sictiam.stela.acteservice.model.NotificationValue;
import fr.sictiam.stela.acteservice.model.StatusType;
import fr.sictiam.stela.acteservice.model.event.ActeHistoryEvent;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
    TemplateEngine template;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Value("${application.url}")
    private String applicationUrl;

    @Override
    public void onApplicationEvent(@NotNull ActeHistoryEvent event) {
        List<String> notificationTypes = Notification.notifications.stream().map(n -> n.getType().toString())
                .collect(Collectors.toList());
        if (notificationTypes.contains(event.getActeHistory().getStatus().toString())
                || event.getActeHistory().getStatus().isAnomaly()) {
            try {
                proccessEvent(event);
            } catch (MessagingException | IOException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }

    public void proccessEvent(ActeHistoryEvent event) throws MessagingException, IOException {
        Acte acte = acteService.getByUuid(event.getActeHistory().getActeUuid());

        Notification notification = Notification.notifications.stream().filter(
                n -> n.getType().toString().equals(event.getActeHistory().getStatus().isAnomaly() ?
                        Notification.Type.ANOMALIES.toString() :
                        event.getActeHistory().getStatus().toString())
        ).findFirst().get();

        JsonNode profiles = externalRestService.getProfiles(acte.getLocalAuthority().getUuid());

        AtomicInteger notifcationSentNumber = new AtomicInteger(0);
        profiles.forEach(profile -> {
            if (StreamSupport.stream(profile.get("localAuthorityNotifications").spliterator(), false)
                    .anyMatch(value -> value.asText().equals("ACTES"))
                    && !acte.getProfileUuid().equals(profile.get("uuid").asText())) {
                List<NotificationValue> profileNotifications = getNotificationValues(profile);

                if (!notification.isDeactivatable()
                        || profileNotifications.stream()
                        .anyMatch(notif -> notif.getName().equals(event.getActeHistory().getStatus().toString())
                                && notif.isActive())
                        || (event.getActeHistory().getStatus().isAnomaly() &&
                        profileNotifications.stream()
                                .anyMatch(notif -> notif.getName().equals(Notification.Type.ANOMALIES.toString())))
                        || (notification.isDefaultValue() && profileNotifications.isEmpty())) {
                    try {
                        sendMailWithMessage(acte, event, profile, false);
                        notifcationSentNumber.incrementAndGet();
                    } catch (MessagingException | IOException e) {
                        LOGGER.error(e.getMessage());
                    }
                }
            }
        });
        if (notifcationSentNumber.get() > 0) {
            ActeHistory acteHistory = new ActeHistory(acte.getUuid(), StatusType.GROUP_NOTIFICATION_SENT);
            applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory));
        }

        if (StringUtils.isNotBlank(acte.getProfileUuid())) {
            JsonNode node = externalRestService.getProfile(acte.getProfileUuid());
            List<NotificationValue> notifications = getNotificationValues(node);

            if (!notification.isDeactivatable()
                    || notifications.stream()
                    .anyMatch(notif -> notif.getName().equals(event.getActeHistory().getStatus().toString())
                            && notif.isActive())
                    || (event.getActeHistory().getStatus().isAnomaly() &&
                    notifications.stream()
                            .anyMatch(notif -> notif.getName().equals(Notification.Type.ANOMALIES.toString())))
                    || (notification.isDefaultValue() && notifications.isEmpty())) {
                sendMailWithMessage(acte, event, node, false);
                if (notification.isNotificationStatus()) {
                    ActeHistory acteHistory = new ActeHistory(acte.getUuid(), StatusType.NOTIFICATION_SENT);
                    applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory));
                }
            }
        }

    }

    private void sendMailWithMessage(Acte acte, ActeHistoryEvent event, JsonNode profileNode, boolean isCopy)
            throws IOException, MessagingException {
        Context ctx = new Context(Locale.FRENCH, getAgentInfo(profileNode));
        ctx.setVariable("acte", acte);
        ctx.setVariable("baseUrl", applicationUrl);
        ctx.setVariable("localAuthority", acte.getLocalAuthority().getSlugName());
        StatusType status = event.getActeHistory().getStatus();
        String msg = template.process("mails/" + status.name() + "_fr", ctx);
        sendMail(getAgentMail(profileNode),
                localesService.getMessage("fr", "acte_notification",
                        (isCopy ? "$.acte.copy." : "$.acte.")
                                + (status.isAnomaly() ? Notification.Type.ANOMALIES.toString() : status.name())
                                + ".subject"),
                msg);
    }

    public List<NotificationValue> getNotificationValues(JsonNode node) {
        List<NotificationValue> notifications = new ArrayList<>();
        node.get("notificationValues").forEach(notif -> {
            if (StringUtils.startsWith(notif.get("name").asText(), "ACTES_"))
                notifications.add(new NotificationValue(notif.get("uuid").asText(),
                        StringUtils.removeStart(notif.get("name").asText(), "ACTES_"),
                        notif.get("active").asBoolean()));
        });
        return notifications;
    }

    public void sendMail(String mail, String subject, String text) throws MessagingException, IOException {

        String[] mails = {mail};
        sendMail(mails, subject, text);
    }

    public void sendMail(String[] mails, String subject, String text) throws MessagingException, IOException {

        MimeMessage message = emailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setSubject(subject);
        helper.setText(text, true);
        helper.setTo(mails);

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

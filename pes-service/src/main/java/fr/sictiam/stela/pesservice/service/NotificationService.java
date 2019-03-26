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
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static fr.sictiam.stela.pesservice.service.util.JsonExtractorUtils.*;

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
        List<String> notificationTypes = Notification.notifications.stream()
                .map(n -> n.getType().toString())
                .collect(Collectors.toList());
        if (notificationTypes.contains(event.getPesHistory().getStatus().toString())
                || event.getPesHistory().getStatus().isAnomaly()) {
            try {
                proccessEvent(event);
            } catch (IOException e) {
                LOGGER.error("[onApplicationEvent] An error occured while trying to process event '{}' for pes {} : {}",
                        event.getPesHistory().getStatus().toString(),
                        event.getPesHistory().getPesUuid(),
                        e.getMessage());
            }
        }
    }

    public void proccessEvent(PesHistoryEvent event) throws IOException {
        PesAller pes = pesService.getByUuid(event.getPesHistory().getPesUuid());

        LOGGER.info("[proccessEvent] Proccess notification event for pes {}", pes.getUuid());

        Optional<Notification> notification = Notification.notifications.stream()
                .filter(n -> n.getType().toString().equals(event.getPesHistory().getStatus().isAnomaly() ?
                        Notification.Type.ANOMALIES.toString() :
                        event.getPesHistory().getStatus().toString())
                )
                .findFirst();

        JsonNode profiles = externalRestService.getProfiles(pes.getLocalAuthority().getUuid());

        AtomicInteger notifcationSentNumber = new AtomicInteger(0);
        profiles.forEach(profile -> {
            if (StreamSupport.stream(profile.get("localAuthorityNotifications").spliterator(), false)
                    .anyMatch(value -> value.asText().equals("PES"))
                    && !pes.getProfileUuid().equals(profile.get("uuid").asText())) {
                List<NotificationValue> profileNotifications = getNotificationValues(profile);

                if (this.verifyProfileNotificationSubscription(notification, profileNotifications, event.getPesHistory())) {
                    try {
                        sendMailWithMessage(pes, event, profile, false);
                        notifcationSentNumber.incrementAndGet();
                        LOGGER.info(
                                "[proccessEvent] An email '{}' notification was sent to {} for pes {} ",
                                notification.get().getType().toString(),
                                extractEmailFromProfile(profile),
                                pes.getUuid());
                    } catch (MessagingException | IOException e) {
                        LOGGER.error(
                                "[proccessEvent] An error are occurred when trying to send '{}' mail notification for pes {} : {}",
                                notification.get().getType().toString(),
                                pes.getUuid(),
                                e.getMessage());
                    }
                }
            }
        });

        if (notifcationSentNumber.get() > 0) {
            PesHistory pesHistory = new PesHistory(pes.getUuid(), StatusType.GROUP_NOTIFICATION_SENT);
            applicationEventPublisher.publishEvent(new PesHistoryEvent(this, pesHistory));
        }

        if (StringUtils.isNotBlank(pes.getProfileUuid())) {
            JsonNode node = externalRestService.getProfile(pes.getProfileUuid());
            List<NotificationValue> notifications = getNotificationValues(node);

            if (this.verifyProfileNotificationSubscription(notification, notifications, event.getPesHistory())) {
                try {
                    sendMailWithMessage(pes, event, node, false);
                    LOGGER.info(
                            "[proccessEvent] An email '{}' notification was sent to {} for pes {} ",
                            notification.get().getType().toString(),
                            extractEmailFromProfile(node),
                            pes.getUuid());
                    if (notification.get().isNotificationStatus()) {
                        PesHistory pesHistory = new PesHistory(pes.getUuid(), StatusType.NOTIFICATION_SENT);
                        pesService.updateHistory(pesHistory);
                        applicationEventPublisher.publishEvent(new PesHistoryEvent(this, pesHistory));
                    }
                } catch (MessagingException | IOException e) {
                    LOGGER.error(
                            "[proccessEvent] An error are occurred when trying to send '{}' mail notification for pes {} : {}",
                            notification.get().getType().toString(),
                            pes.getUuid(),
                            e.getMessage());
                }
            }
        }

    }

    private void sendMailWithMessage(PesAller pes, PesHistoryEvent event, JsonNode profileNode, boolean isCopy)
            throws IOException, MessagingException {
        Context ctx = new Context(Locale.FRENCH, getAgentInfo(profileNode));
        ctx.setVariable("pes", pes);
        ctx.setVariable("errors", event.getPesHistory().getErrors());
        ctx.setVariable("baseUrl", applicationUrl);
        ctx.setVariable("localAuthority", pes.getLocalAuthority().getSlugName());
        StatusType status = event.getPesHistory().getStatus();
        String msg = template.process("mails/" + status.name() + "_fr", ctx);
        sendMail(extractEmailFromProfile(profileNode),
                localesService.getMessage("fr", "pes_notification",
                        (isCopy ? "$.pes.copy." : "$.pes.")
                                + (status.isAnomaly() ? Notification.Type.ANOMALIES.toString() : status.name())
                                + ".subject"),
                msg);
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

    public Map<String, Object> getAgentInfo(JsonNode node) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("firstname", node.get("agent").get("given_name").asText());
        variables.put("lastname", node.get("agent").get("family_name").asText());
        return variables;
    }

    private boolean verifyProfileNotificationSubscription(Optional<Notification> notification, List<NotificationValue> notificationValues, PesHistory pesHistory) {
        if(notification.isPresent()) {
            return (!notification.get().isDeactivatable()
                    || notificationValues.stream()
                    .anyMatch(notif -> notif.getName().equals(pesHistory.getStatus().toString())
                            && notif.isActive())
                    || (pesHistory.getStatus().isAnomaly() &&
                    notificationValues.stream()
                            .anyMatch(notif -> notif.getName().equals(Notification.Type.ANOMALIES.toString())))
                    || (notification.get().isDefaultValue() && notificationValues.isEmpty()));
        } else {
            LOGGER.debug(
                    "[proccessEvent] Something wrong for retrieve notification for this event {} pes {} ",
                    pesHistory.getStatus().toString(),
                    pesHistory.getPesUuid());
            return false;
        }
    }
}
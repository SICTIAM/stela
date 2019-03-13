package fr.sictiam.stela.convocationservice.service.eventlistener;

import fr.sictiam.stela.convocationservice.model.Convocation;
import fr.sictiam.stela.convocationservice.model.MailTemplate;
import fr.sictiam.stela.convocationservice.model.Notification;
import fr.sictiam.stela.convocationservice.model.NotificationType;
import fr.sictiam.stela.convocationservice.model.NotificationValue;
import fr.sictiam.stela.convocationservice.model.Profile;
import fr.sictiam.stela.convocationservice.model.Recipient;
import fr.sictiam.stela.convocationservice.model.RecipientResponse;
import fr.sictiam.stela.convocationservice.model.event.notifications.ConvocationCreatedEvent;
import fr.sictiam.stela.convocationservice.model.event.notifications.ConvocationReadEvent;
import fr.sictiam.stela.convocationservice.model.event.notifications.ConvocationRecipientAddedEvent;
import fr.sictiam.stela.convocationservice.model.event.notifications.ConvocationUpdatedEvent;
import fr.sictiam.stela.convocationservice.service.ConvocationService;
import fr.sictiam.stela.convocationservice.service.LocalesService;
import fr.sictiam.stela.convocationservice.service.MailTemplateService;
import fr.sictiam.stela.convocationservice.service.MailerService;
import fr.sictiam.stela.convocationservice.service.exceptions.MailException;
import org.apache.commons.lang.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class NotificationEventListener {

    private final static Logger LOGGER = LoggerFactory.getLogger(NotificationEventListener.class);

    private final MailerService mailerService;

    private final ConvocationService convocationService;

    private final MailTemplateService mailTemplateService;

    private final LocalesService localesService;

    @Value("${application.url}")
    private String applicationUrl;

    @Autowired
    public NotificationEventListener(
            MailerService mailerService,
            ConvocationService convocationService,
            MailTemplateService mailTemplateService,
            LocalesService localesService) {
        this.mailerService = mailerService;
        this.convocationService = convocationService;
        this.mailTemplateService = mailTemplateService;
        this.localesService = localesService;
    }


    @EventListener
    @Async
    public void convocationCreated(ConvocationCreatedEvent event) {

        // get full object from DB
        Convocation convocation = convocationService.getConvocation(event.getConvocation().getUuid());

        sendToRecipients(convocation, NotificationType.CONVOCATION_CREATED, convocation.getRecipientResponses());

        convocationService.convocationSent(convocation);
        LOGGER.info("Creation notification sent for convocation {} ({})", convocation.getUuid(), convocation.getSubject());
    }

    @EventListener
    @Async
    public void convocationUpdated(ConvocationUpdatedEvent event) {

        // get full object from DB
        Convocation convocation = convocationService.getConvocation(event.getConvocation().getUuid());
        List<String> updates = event.getUpdates();

        sendToRecipients(convocation, NotificationType.CONVOCATION_UPDATED, convocation.getRecipientResponses(),
                updates);

        LOGGER.info("Update notification sent for convocation {} ({})", convocation.getUuid(),
                convocation.getSubject());
    }

    @EventListener
    @Async
    public void recipientsAdded(ConvocationRecipientAddedEvent event) {

        // get full object from DB
        Convocation convocation = convocationService.getConvocation(event.getConvocation().getUuid());
        Set<Recipient> recipients = event.getRecipients();

        // extract convocation RecipientResponse from recipient list
        Set<RecipientResponse> newRecipients = convocation.getRecipientResponses()
                .stream()
                .filter(recipientResponse -> recipients.stream().anyMatch(recipient -> recipientResponse.getRecipient().equals(recipient)))
                .collect(Collectors.toSet());

        sendToRecipients(convocation, NotificationType.CONVOCATION_CREATED, newRecipients);

        LOGGER.info("Added recipients notification sent for convocation {} ({})", convocation.getUuid(),
                convocation.getSubject());
    }

    @EventListener
    @Async
    public void convocationReadByRecipient(ConvocationReadEvent event) {

        Convocation convocation = event.getConvocation();
        RecipientResponse recipientResponse = event.getRecipientResponse();

        Profile author = convocationService.retrieveProfile(convocation.getProfileUuid());

        if (hasNotificationActive(author, NotificationType.CONVOCATION_READ)) {
            MailTemplate template = mailTemplateService.getTemplate(NotificationType.CONVOCATION_READ,
                    convocation.getLocalAuthority());

            String body = StrSubstitutor.replace(
                    template.getBody(),
                    buildPlaceHolderMap(convocation, author, recipientResponse, null, false),
                    "{{",
                    "}}");
            try {
                mailerService.sendEmail(author.getEmail(), template.getSubject(), body);
                LOGGER.info("Read notification sent for convocation {} ({})", convocation.getUuid(),
                        convocation.getSubject());
            } catch (MailException e) {
                LOGGER.error("Error while sending notification {} to {}: {}",
                        NotificationType.CONVOCATION_READ.name(), author.getEmail(), e.getMessage());
                // TODO: maybe a retry process
            }
        } else {
            LOGGER.debug("{} ({}) did not subscribe to {} notifications", author.getFullName(), author.getUuid(),
                    NotificationType.CONVOCATION_READ);
        }
    }

    private Map<String, String> buildPlaceHolderMap(
            Convocation convocation,
            Profile author,
            RecipientResponse recipientResponse,
            List<String> updates,
            boolean received) {

        Map<String, String> placeHolders = new HashMap<>();
        placeHolders.put("sujet", convocation.getSubject());

        String url = String.format("%s/%s/convocation/%s/%s?token=%s", applicationUrl,
                convocation.getLocalAuthority().getSlugName(),
                (received ? "liste-recues" : "liste-envoyees"),
                convocation.getUuid(), recipientResponse.getRecipient().getToken());
        placeHolders.put("convocation", url);

        placeHolders.put("stela_url", applicationUrl);
        placeHolders.put("collectivite", convocation.getLocalAuthority().getName());

        placeHolders.put("destinataire", recipientResponse.getRecipient().getFullName());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        placeHolders.put("date", formatter.format(convocation.getMeetingDate()));

        if (author != null)
            placeHolders.put("emetteur", author.getFullName());

        if (recipientResponse.getSubstituteRecipient() != null)
            placeHolders.put("mandataire", recipientResponse.getSubstituteRecipient().getFullName());

        if (updates != null) {
            placeHolders.put("modifications",
                    updates.stream().map(s -> "<li>" + localesService.getMessage("fr", "convocation", "$.convocation" +
                            ".notifications." + s) + "</li>").collect(Collectors.joining()));
        }

        return placeHolders;
    }

    private void sendToRecipients(Convocation convocation, NotificationType type,
            Set<RecipientResponse> recipientResponses, List<String> updates) {

        MailTemplate template = mailTemplateService.getTemplate(type, convocation.getLocalAuthority());

        Profile author = convocationService.retrieveProfile(convocation.getProfileUuid());

        for (RecipientResponse recipientResponse : recipientResponses) {
            if (recipientResponse.getRecipient().getActive()) {
                String body = StrSubstitutor.replace(
                        template.getBody(),
                        buildPlaceHolderMap(convocation, author,
                                recipientResponse, updates, true),
                        "{{",
                        "}}");
                String address = recipientResponse.getRecipient().getEmail();
                try {
                    mailerService.sendEmail(address, template.getSubject(), body, author);
                } catch (MailException e) {
                    LOGGER.error("Error while sending notification {} to {}: {}",
                            type.name(), address, e.getMessage());
                    // TODO: maybe a retry process
                }
            }
        }
    }

    private void sendToRecipients(Convocation convocation, NotificationType type,
            Set<RecipientResponse> recipientResponses) {
        sendToRecipients(convocation, type, recipientResponses, null);
    }

    private boolean hasNotificationActive(Profile author, NotificationType type) {

        Optional<NotificationValue> activeValue = author.getNotificationValues()
                .stream()
                .filter(value -> value.getName().equals(type.name()))
                .findFirst();

        if (activeValue.isPresent()) {
            return activeValue.get().isActive();
        } else {
            // Notification configuration not found in author profile, search default value
            Optional<Notification> activeNotif = Notification.notifications
                    .stream()
                    .filter(notification -> notification.getType() == type && notification.isDefaultValue())
                    .findFirst();
            return activeNotif.isPresent();
        }
    }
}

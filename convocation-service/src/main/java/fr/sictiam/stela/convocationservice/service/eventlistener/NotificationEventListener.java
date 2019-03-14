package fr.sictiam.stela.convocationservice.service.eventlistener;

import fr.sictiam.stela.convocationservice.model.Convocation;
import fr.sictiam.stela.convocationservice.model.MailTemplate;
import fr.sictiam.stela.convocationservice.model.Notification;
import fr.sictiam.stela.convocationservice.model.NotificationType;
import fr.sictiam.stela.convocationservice.model.NotificationValue;
import fr.sictiam.stela.convocationservice.model.Profile;
import fr.sictiam.stela.convocationservice.model.Recipient;
import fr.sictiam.stela.convocationservice.model.RecipientResponse;
import fr.sictiam.stela.convocationservice.model.ResponseType;
import fr.sictiam.stela.convocationservice.model.event.notifications.*;
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
import java.util.Collections;
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
    public void convocationCancelled(ConvocationCancelledEvent event) {
        // get full object from DB
        Convocation convocation = convocationService.getConvocation(event.getConvocation().getUuid());

        sendToRecipients(convocation, NotificationType.CONVOCATION_CANCELLED, convocation.getRecipientResponses());

        LOGGER.info("Cancelled notification sent for convocation {} ({})", convocation.getUuid(),
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
    public void procurationReceived(ProcurationReceivedEvent event) {

        Convocation convocation = convocationService.getConvocation(event.getConvocation().getUuid());
        RecipientResponse recipientResponse = event.getRecipientResponse();

        sendToRecipients(convocation, NotificationType.PROCURATION_RECEIVED, Collections.singleton(recipientResponse));

        LOGGER.info("Procuration received notification sent for convocation {} ({})", convocation.getUuid(),
                convocation.getSubject());
    }

    @EventListener
    @Async
    public void procurationReceived(ProcurationCancelledEvent event) {

        Convocation convocation = convocationService.getConvocation(event.getConvocation().getUuid());
        RecipientResponse recipientResponse = event.getRecipientResponse();

        sendToRecipients(convocation, NotificationType.PROCURATION_CANCELLED, Collections.singleton(recipientResponse));

        LOGGER.info("Procuration cancelled notification sent for convocation {} ({})", convocation.getUuid(),
                convocation.getSubject());
    }

    @EventListener
    @Async
    public void convocationReadByRecipient(ConvocationReadEvent event) {

        Convocation convocation = event.getConvocation();
        RecipientResponse recipientResponse = event.getRecipientResponse();

        sendToAuthor(convocation, recipientResponse, NotificationType.CONVOCATION_READ);
    }

    @EventListener
    @Async
    public void convocationResponse(ConvocationResponseEvent event) {

        Convocation convocation = event.getConvocation();
        RecipientResponse recipientResponse = event.getRecipientResponse();

        sendToAuthor(convocation, recipientResponse, NotificationType.CONVOCATION_RESPONSE);
    }

    @EventListener
    @Async
    public void reminderNotification(ReminderEvent event) {

        Convocation convocation = event.getConvocation();

        Set<RecipientResponse> recipientResponses = convocation.getRecipientResponses()
                .stream()
                .filter(recipientResponse -> recipientResponse.getResponseType() == ResponseType.DO_NOT_KNOW)
                .collect(Collectors.toSet());

        recipientResponses.forEach(recipientResponse -> LOGGER.debug("Recipient {} has not answer to convocation {}",
                recipientResponse.getRecipient().getEmail(), convocation.getSubject()));

        sendToRecipients(convocation, NotificationType.CONVOCATION_REMINDER, recipientResponses);

        if (recipientResponses.size() > 0) {
            LOGGER.info("Reminder notification sent for convocation {} ({})", convocation.getUuid(),
                    convocation.getSubject());
        }
    }

    @EventListener
    @Async
    public void noResponseInfo(NoResponseInfoEvent event) {

        Convocation convocation = event.getConvocation();

        List<String> recipients = convocation
                .getRecipientResponses()
                .stream()
                .filter(r -> r.getResponseType() == ResponseType.DO_NOT_KNOW)
                .map(r -> r.getRecipient().getFullName())
                .collect(Collectors.toList());

        sendToAuthor(convocation, null, recipients, NotificationType.NO_RESPONSE_INFO);
    }

    private void sendToAuthor(Convocation convocation, RecipientResponse recipientResponse, NotificationType type) {

        sendToAuthor(convocation, recipientResponse, null, type);
    }

    private void sendToAuthor(Convocation convocation, RecipientResponse recipientResponse,
            List<String> recipients, NotificationType type) {

        Profile author = convocationService.retrieveProfile(convocation.getProfileUuid());

        if (hasNotificationActive(author, type)) {
            MailTemplate template = mailTemplateService.getTemplate(type, convocation.getLocalAuthority());

            String body = StrSubstitutor.replace(
                    template.getBody(),
                    buildPlaceHolderMap(convocation, author, recipientResponse, null, recipients, false),
                    "{{",
                    "}}");
            try {
                mailerService.sendEmail(author.getEmail(), template.getSubject(), body);
                LOGGER.info("{} notification sent for convocation {} ({})", type.name(), convocation.getUuid(),
                        convocation.getSubject());
            } catch (MailException e) {
                LOGGER.error("Error while sending notification {} to {}: {}",
                        type.name(), author.getEmail(), e.getMessage());
                // TODO: maybe a retry process
            }
        } else {
            LOGGER.debug("{} ({}) did not subscribe to {} notifications", author.getFullName(), author.getUuid(), type);
        }
    }

    private Map<String, String> buildPlaceHolderMap(
            Convocation convocation,
            Profile author,
            RecipientResponse recipientResponse,
            List<String> updates,
            List<String> recipients,
            boolean received) {

        Map<String, String> placeHolders = new HashMap<>();
        placeHolders.put("sujet", convocation.getSubject());

        String url = String.format("%s/%s/convocation/%s/%s", applicationUrl,
                convocation.getLocalAuthority().getSlugName(),
                (received ? "liste-recues" : "liste-envoyees"),
                convocation.getUuid());

        if (recipientResponse != null && received) {
            url += "?token=" + (recipientResponse.getResponseType() == ResponseType.SUBSTITUTED && recipientResponse.getSubstituteRecipient() != null ?
                    recipientResponse.getSubstituteRecipient().getToken() :
                    recipientResponse.getRecipient().getToken());
        }

        if (recipientResponse != null) {
            placeHolders.put("destinataire", recipientResponse.getRecipient().getFullName());
            placeHolders.put("reponse", recipientResponse.getResponseType() == ResponseType.DO_NOT_KNOW ?
                    "" :
                    localesService.getMessage("fr", "convocation",
                            "$.convocation.notifications." + recipientResponse.getResponseType().name()));

            if (recipientResponse.getSubstituteRecipient() != null)
                placeHolders.put("mandataire", recipientResponse.getSubstituteRecipient().getFullName());
        }

        placeHolders.put("convocation", url);

        placeHolders.put("stela_url", applicationUrl);
        placeHolders.put("collectivite", convocation.getLocalAuthority().getName());


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        placeHolders.put("date", formatter.format(convocation.getMeetingDate()));

        if (author != null)
            placeHolders.put("emetteur", author.getFullName());


        if (updates != null) {
            placeHolders.put("modifications",
                    updates.stream().map(s -> "<li>" + localesService.getMessage("fr", "convocation", "$.convocation" +
                            ".notifications." + s) + "</li>").collect(Collectors.joining()));
        }

        if (recipients != null) {
            placeHolders.put("destinataires",
                    recipients.stream().map(s -> "<li>" + s + "</li>").collect(Collectors.joining()));
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
                                recipientResponse, updates, null, true),
                        "{{",
                        "}}");
                String address =
                        recipientResponse.getResponseType().equals(ResponseType.SUBSTITUTED) && recipientResponse.getSubstituteRecipient() != null ?
                                recipientResponse.getSubstituteRecipient().getEmail() :
                                recipientResponse.getRecipient().getEmail();
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

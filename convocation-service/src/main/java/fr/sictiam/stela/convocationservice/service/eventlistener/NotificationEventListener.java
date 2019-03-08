package fr.sictiam.stela.convocationservice.service.eventlistener;

import fr.sictiam.stela.convocationservice.model.Convocation;
import fr.sictiam.stela.convocationservice.model.MailTemplate;
import fr.sictiam.stela.convocationservice.model.NotificationType;
import fr.sictiam.stela.convocationservice.model.Profile;
import fr.sictiam.stela.convocationservice.model.Recipient;
import fr.sictiam.stela.convocationservice.model.RecipientResponse;
import fr.sictiam.stela.convocationservice.model.event.notifications.ConvocationCreatedEvent;
import fr.sictiam.stela.convocationservice.model.event.notifications.ConvocationRecipientAddedEvent;
import fr.sictiam.stela.convocationservice.model.event.notifications.ConvocationUpdatedEvent;
import fr.sictiam.stela.convocationservice.service.ConvocationService;
import fr.sictiam.stela.convocationservice.service.MailTemplateService;
import fr.sictiam.stela.convocationservice.service.MailerService;
import fr.sictiam.stela.convocationservice.service.StorageService;
import fr.sictiam.stela.convocationservice.service.exceptions.MailException;
import org.apache.commons.lang.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class NotificationEventListener {

    private final static Logger LOGGER = LoggerFactory.getLogger(NotificationEventListener.class);

    private final MailerService mailerService;

    private final ConvocationService convocationService;

    private final MailTemplateService mailTemplateService;

    private final StorageService storageService;

    @Value("${application.url}")
    private String applicationUrl;

    @Autowired
    public NotificationEventListener(
            MailerService mailerService,
            ConvocationService convocationService,
            MailTemplateService mailTemplateService,
            StorageService storageService) {
        this.mailerService = mailerService;
        this.convocationService = convocationService;
        this.mailTemplateService = mailTemplateService;
        this.storageService = storageService;
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

        sendToRecipients(convocation, NotificationType.CONVOCATION_UPDATED, convocation.getRecipientResponses());

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

    private void sendToRecipients(Convocation convocation, NotificationType type,
            Set<RecipientResponse> recipientResponses) {

        MailTemplate template = mailTemplateService.getTemplate(type, convocation.getLocalAuthority());

        Profile author = convocationService.retrieveProfile(convocation.getProfileUuid());

        for (RecipientResponse recipientResponse : recipientResponses) {
            if (recipientResponse.getRecipient().getActive()) {
                String body = StrSubstitutor.replace(template.getBody(), buildPlaceHolderMap(convocation,
                        recipientResponse.getRecipient(), null, true));
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

    private Map<String, String> buildPlaceHolderMap(Convocation convocation, Recipient recipient,
            Recipient substitute, boolean received) {

        Map<String, String> placeHolders = new HashMap<>();
        placeHolders.put("name", convocation.getSubject());

        String url = String.format("%s/%s/convocation%s/%s?token=%s", applicationUrl,
                convocation.getLocalAuthority().getSlugName(),
                (received ? "/liste-recues" : ""),
                convocation.getUuid(), recipient.getToken());
        placeHolders.put("url", "<a href='" + url + "'>" + url + "</a>");

        if (recipient != null)
            placeHolders.put("recipient", recipient.getFullName());

        if (substitute != null)
            placeHolders.put("substitute", substitute.getFullName());

        return placeHolders;
    }
}

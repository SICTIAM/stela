package fr.sictiam.stela.convocationservice.controller;

import fr.sictiam.stela.convocationservice.model.LocalAuthority;
import fr.sictiam.stela.convocationservice.model.MailTemplate;
import fr.sictiam.stela.convocationservice.model.Notification;
import fr.sictiam.stela.convocationservice.model.NotificationType;
import fr.sictiam.stela.convocationservice.model.Placeholder;
import fr.sictiam.stela.convocationservice.model.Right;
import fr.sictiam.stela.convocationservice.model.util.RightUtils;
import fr.sictiam.stela.convocationservice.service.LocalAuthorityService;
import fr.sictiam.stela.convocationservice.service.MailTemplateService;
import fr.sictiam.stela.convocationservice.service.exceptions.MailException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


@RestController
@RequestMapping("/api/convocation/notifications")
public class NotificationRestController {

    private final MailTemplateService mailTemplateService;

    private final LocalAuthorityService localAuthorityService;

    @Autowired
    public NotificationRestController(
            MailTemplateService mailTemplateService,
            LocalAuthorityService localAuthorityService) {
        this.mailTemplateService = mailTemplateService;
        this.localAuthorityService = localAuthorityService;
    }

    @GetMapping("/all")
    public List<Notification> getAllNotifications() {
        return Notification.notifications;
    }

    @GetMapping("/mails")
    public ResponseEntity<List<MailTemplate>> getMails(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute(name = "STELA-Current-Profile-UUID") String profileUuid) {

        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.CONVOCATION_ADMIN))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        LocalAuthority localAuthority = localAuthorityService.getLocalAuthority(currentLocalAuthUuid);

        List<MailTemplate> mails = mailTemplateService.getTemplates(localAuthority);

        return new ResponseEntity<>(mails, HttpStatus.OK);
    }

    @PostMapping("/mail")
    public ResponseEntity<?> saveMail(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestBody MailTemplate params) {

        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.CONVOCATION_ADMIN))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if (params.getLocalAuthorityUuid() != null && !params.getLocalAuthorityUuid().equals(currentLocalAuthUuid)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            MailTemplate mailTemplate = mailTemplateService.saveTemplate(params, currentLocalAuthUuid);
            return new ResponseEntity<>(mailTemplate, HttpStatus.OK);
        } catch (MailException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    @GetMapping("/mail/{type}")
    public ResponseEntity<MailTemplate> getMail(
            @RequestAttribute("STELA-Current-Profile-Rights") Set<Right> rights,
            @RequestAttribute("STELA-Current-Local-Authority-UUID") String currentLocalAuthUuid,
            @RequestAttribute("STELA-Current-Profile-UUID") String profileUuid,
            @PathVariable NotificationType type) {

        if (!RightUtils.hasRight(rights, Collections.singletonList(Right.CONVOCATION_ADMIN))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        LocalAuthority localAuthority = localAuthorityService.getLocalAuthority(currentLocalAuthUuid);

        MailTemplate template = mailTemplateService.getTemplate(type, localAuthority);

        return new ResponseEntity<>(template, HttpStatus.OK);
    }

    @GetMapping("/placeholders")
    public List<Placeholder> getPlaceholderList() {

        List<Placeholder> placeholders = new ArrayList<>();


        placeholders.add(new Placeholder("sujet", "Sujet de la convocation"));
        placeholders.add(new Placeholder("convocation", "URL de la convocation"));
        placeholders.add(new Placeholder("destinataire", "Destinataire principal"));
        placeholders.add(new Placeholder("emetteur", "Déposant de la convocation"));
        placeholders.add(new Placeholder("mandataire", "Destinataire ayant reçu une procuration"));
        placeholders.add(new Placeholder("reponse", "Réponse du destinataire"));
        placeholders.add(new Placeholder("date", "Date de séance"));
        placeholders.add(new Placeholder("collectivite", "Collectivité émettrice de la convocation"));
        placeholders.add(new Placeholder("modifications", "Liste des modifications apportées à une convocation"));
        placeholders.add(new Placeholder("destinataires", "Liste de destinataires"));
        placeholders.add(new Placeholder("stela_url", "URL de Stela"));

        return placeholders;
    }
}
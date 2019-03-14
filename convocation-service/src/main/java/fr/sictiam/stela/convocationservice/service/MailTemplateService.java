package fr.sictiam.stela.convocationservice.service;

import fr.sictiam.stela.convocationservice.dao.MailTemplateRepository;
import fr.sictiam.stela.convocationservice.model.LocalAuthority;
import fr.sictiam.stela.convocationservice.model.MailTemplate;
import fr.sictiam.stela.convocationservice.model.NotificationType;
import fr.sictiam.stela.convocationservice.model.exception.NotFoundException;
import fr.sictiam.stela.convocationservice.service.exceptions.MailException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MailTemplateService {

    private final static Logger LOGGER = LoggerFactory.getLogger(MailTemplateService.class);

    private final MailTemplateRepository mailTemplateRepository;

    @Autowired
    public MailTemplateService(MailTemplateRepository mailTemplateRepository) {
        this.mailTemplateRepository = mailTemplateRepository;
    }

    public MailTemplate getTemplate(NotificationType type, LocalAuthority localAuthority) {

        Optional<MailTemplate> opt =
                mailTemplateRepository.findByNotificationTypeAndLocalAuthorityUuid(type, localAuthority.getUuid());

        if (!opt.isPresent()) {
            LOGGER.debug("No specific template for notification {} and local authority {}. Return default template",
                    type.name(), localAuthority.getUuid());
            opt = mailTemplateRepository
                    .findByNotificationTypeAndLocalAuthorityUuid(type, null);
        }

        return opt.orElseThrow(NotFoundException::new);
    }

    public List<MailTemplate> getTemplates(LocalAuthority localAuthority) {

        List<MailTemplate> templates = new ArrayList<>();

        for (NotificationType type : NotificationType.values()) {
            templates.add(getTemplate(type, localAuthority));
        }
        return templates;
    }

    public MailTemplate saveTemplate(MailTemplate template, String localAuthorityUuid) throws MailException {

        if (StringUtils.isBlank(template.getLocalAuthorityUuid())) {
            if (templateExists(template.getNotificationType(), localAuthorityUuid)) {
                throw new MailException("convocation.errors.convocation.mailAlreadyCustomized");
            }
            // Save as new template for current local authority
            template = new MailTemplate(
                    template.getNotificationType(),
                    template.getSubject(),
                    template.getBody(),
                    localAuthorityUuid);
        }
        return mailTemplateRepository.saveAndFlush(template);
    }

    public boolean templateExists(NotificationType type, String localAuthorityUuid) {

        Optional<MailTemplate> opt =
                mailTemplateRepository.findByNotificationTypeAndLocalAuthorityUuid(type, localAuthorityUuid);

        return opt.isPresent();
    }
}

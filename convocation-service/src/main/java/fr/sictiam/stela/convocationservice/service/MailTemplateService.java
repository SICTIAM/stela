package fr.sictiam.stela.convocationservice.service;

import fr.sictiam.stela.convocationservice.dao.MailTemplateRepository;
import fr.sictiam.stela.convocationservice.model.LocalAuthority;
import fr.sictiam.stela.convocationservice.model.MailTemplate;
import fr.sictiam.stela.convocationservice.model.NotificationType;
import fr.sictiam.stela.convocationservice.model.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
                mailTemplateRepository.findByNotificationTypeAndLocalAuthorityUuid(NotificationType.CONVOCATION_CREATED, localAuthority.getUuid());

        if (!opt.isPresent()) {
            LOGGER.info("No specific template for notification {} and local authority {}. Return default template",
                    type.name(), localAuthority.getUuid());
            opt = mailTemplateRepository
                    .findByNotificationTypeAndLocalAuthorityUuid(NotificationType.CONVOCATION_CREATED, null);
        }

        return opt.orElseThrow(NotFoundException::new);
    }
}

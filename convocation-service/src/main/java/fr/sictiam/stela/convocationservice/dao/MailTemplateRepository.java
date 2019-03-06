package fr.sictiam.stela.convocationservice.dao;

import fr.sictiam.stela.convocationservice.model.MailTemplate;
import fr.sictiam.stela.convocationservice.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MailTemplateRepository extends JpaRepository<MailTemplate, String> {

    public Optional<MailTemplate> findByNotificationTypeAndLocalAuthorityUuid(NotificationType type,
            String localAuthorityUuid);

}

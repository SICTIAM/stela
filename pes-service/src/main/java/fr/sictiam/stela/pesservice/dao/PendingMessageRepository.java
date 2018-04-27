package fr.sictiam.stela.pesservice.dao;

import fr.sictiam.stela.pesservice.model.PendingMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingMessageRepository extends JpaRepository<PendingMessage, String> {
}

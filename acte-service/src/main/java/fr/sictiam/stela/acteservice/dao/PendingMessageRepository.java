package fr.sictiam.stela.acteservice.dao;

import fr.sictiam.stela.acteservice.model.PendingMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingMessageRepository extends JpaRepository<PendingMessage, String> {
}

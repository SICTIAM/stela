package fr.sictiam.stela.pesservice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.sictiam.stela.pesservice.model.PendingMessage;

public interface PendingMessageRepository extends JpaRepository<PendingMessage, String> {
}

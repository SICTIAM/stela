package fr.sictiam.stela.acteservice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.sictiam.stela.acteservice.model.PendingMessage;

public interface PendingMessageRepository extends JpaRepository<PendingMessage, String> {
}

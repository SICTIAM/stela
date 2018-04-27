package fr.sictiam.stela.acteservice.dao;

import fr.sictiam.stela.acteservice.model.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttachmentRepository extends JpaRepository<Attachment, String> {
    Optional<Attachment> findByUuid(String uuid);
}
package fr.sictiam.stela.pesservice.dao;

import fr.sictiam.stela.pesservice.model.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttachmentRepository extends JpaRepository<Attachment, String> {
    Optional<Attachment> findByUuid(String uuid);
}

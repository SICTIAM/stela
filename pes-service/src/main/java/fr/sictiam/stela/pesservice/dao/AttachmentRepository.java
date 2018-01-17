package fr.sictiam.stela.pesservice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.sictiam.stela.pesservice.model.Attachment;

import java.util.Optional;

public interface AttachmentRepository extends JpaRepository<Attachment, String> {
    Optional<Attachment> findByUuid(String uuid);
}

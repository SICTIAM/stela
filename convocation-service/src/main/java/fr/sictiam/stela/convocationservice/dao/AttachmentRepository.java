package fr.sictiam.stela.convocationservice.dao;

import fr.sictiam.stela.convocationservice.model.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, String> {

}

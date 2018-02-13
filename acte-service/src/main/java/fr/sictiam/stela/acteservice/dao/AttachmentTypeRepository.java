package fr.sictiam.stela.acteservice.dao;

import fr.sictiam.stela.acteservice.model.ActeNature;
import fr.sictiam.stela.acteservice.model.AttachmentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttachmentTypeRepository extends JpaRepository<AttachmentType, String> {
    List<AttachmentType> findByAttachmentTypeReferencial_acteNatureAndAttachmentTypeReferencial_localAuthorityUuid(
            ActeNature acteNature, String uuid);
}

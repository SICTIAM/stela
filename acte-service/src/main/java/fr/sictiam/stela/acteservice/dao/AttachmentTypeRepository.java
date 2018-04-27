package fr.sictiam.stela.acteservice.dao;

import fr.sictiam.stela.acteservice.model.ActeNature;
import fr.sictiam.stela.acteservice.model.AttachmentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface AttachmentTypeRepository extends JpaRepository<AttachmentType, String> {
    Set<AttachmentType> findByAttachmentTypeReferencial_acteNatureAndAttachmentTypeReferencial_localAuthorityUuidOrderByLabel(
            ActeNature acteNature, String uuid);
}

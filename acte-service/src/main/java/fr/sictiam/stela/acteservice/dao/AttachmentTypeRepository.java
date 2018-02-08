package fr.sictiam.stela.acteservice.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.sictiam.stela.acteservice.model.ActeNature;
import fr.sictiam.stela.acteservice.model.AttachmentType;

public interface AttachmentTypeRepository extends JpaRepository<AttachmentType, String> {
    List<AttachmentType> findByAttachmentTypeReferencial_acteNatureAndAttachmentTypeReferencial_localAuthorityUuid(
            ActeNature acteNature, String uuid);
}

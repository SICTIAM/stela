package fr.sictiam.stela.acteservice.dao;

import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.model.ActeNature;
import fr.sictiam.stela.acteservice.model.ArchiveStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ActeRepository extends JpaRepository<Acte, String> {

    List<Acte> findAllByDraftNotNullOrderByDraft_LastModifiedDesc();

    Optional<Acte> findFirstByNatureAndLocalAuthorityUuidAndIsPublicWebsiteOrderByDecisionAsc(ActeNature nature,
            String uuid, Boolean isPublicWebsite);

    List<Acte> findAllByDraftNotNullAndDraft_Uuid(String uuid);

    List<Acte> findAllByDraftNullAndMiatIdContainingIgnoreCase(String name);

    Optional<Acte> findByUuidAndDraftNull(String uuid);

    Optional<Acte> findByUuidAndDraftNotNull(String uuid);

    Optional<Acte> findByUuid(String uuid);

    Optional<Acte> findByMiatId(String miatId);

    Optional<Acte> findFirstByNumberAndDecisionAndNatureAndLocalAuthority_UuidAndDraftNull(String number, LocalDate date, ActeNature nature, String localAuthorityUuid);

    Optional<Acte> findByNumberAndLocalAuthoritySirenAndDraftNull(String number, String siren);

    List<Acte> findAllByDraftNullAndLocalAuthorityUuidAndArchiveNull(String uuid);

    List<Acte> findAllByDraftNullAndLocalAuthorityUuidAndArchive_Status(String uuid, ArchiveStatus archiveStatus);

    List<Acte> findByNumberAndLocalAuthorityUuid(String number, String localAuthorityUuid);
}

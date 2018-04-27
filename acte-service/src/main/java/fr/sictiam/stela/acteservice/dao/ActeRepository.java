package fr.sictiam.stela.acteservice.dao;

import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.model.ActeNature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ActeRepository extends JpaRepository<Acte, String> {
    Optional<Acte> findByNumber(String number);

    List<Acte> findAllByDraftNotNullOrderByDraft_LastModifiedDesc();

    Optional<Acte> findFirstByNatureAndLocalAuthorityUuidAndIsPublicWebsiteOrderByDecisionAsc(ActeNature nature,
            String uuid, Boolean isPublicWebsite);

    List<Acte> findAllByDraftNotNullAndDraft_Uuid(String uuid);

    List<Acte> findAllByDraftNullAndMiatIdContainingIgnoreCase(String name);

    Optional<Acte> findByUuidAndDraftNull(String uuid);

    Optional<Acte> findByUuidAndDraftNotNull(String uuid);

    Optional<Acte> findByNumberAndLocalAuthoritySiren(String number, String siren);
}
package fr.sictiam.stela.acteservice.dao;

import java.util.List;
import java.util.Optional;

import fr.sictiam.stela.acteservice.model.Acte;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActeRepository extends JpaRepository<Acte, String> {
    Optional<Acte> findByNumber(String number);
    List<Acte> findAllByDraftNotNullOrderByDraft_LastModifiedDesc();
    Optional<Acte> findByUuidAndDraftNull(String uuid);
    Optional<Acte> findByUuidAndDraftNotNull(String uuid);
}
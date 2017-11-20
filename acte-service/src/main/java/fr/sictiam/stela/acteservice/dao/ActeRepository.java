package fr.sictiam.stela.acteservice.dao;

import java.util.List;
import java.util.Optional;

import fr.sictiam.stela.acteservice.model.Acte;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActeRepository extends JpaRepository<Acte, String> {
    List<Acte> findAllByDraftTrueOrderByCreationDesc();
    Optional<Acte> findByUuidAndDraftFalse(String uuid);
    Optional<Acte> findByUuidAndDraftTrue(String uuid);
    Optional<Acte> findByNumber(String number);
}
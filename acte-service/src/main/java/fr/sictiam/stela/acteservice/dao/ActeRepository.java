package fr.sictiam.stela.acteservice.dao;

import java.util.List;
import java.util.Optional;

import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.model.StatusType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActeRepository extends JpaRepository<Acte, String> {
    List<Acte> findAllByOrderByCreationDesc();
    Optional<Acte> findByUuid(String uuid);
    List<Acte> findByStatus(StatusType created);
}
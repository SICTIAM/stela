package fr.sictiam.stela.acteservice.dao;

import java.util.Optional;

import fr.sictiam.stela.acteservice.model.Acte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ActeRepository extends JpaRepository<Acte, Long> {
    Optional<Acte> findByUuid(Long uuid);
    Optional<Acte> findByNumero(String numero);
}
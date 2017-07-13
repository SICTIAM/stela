package fr.sictiam.stela.acteservice.dao;

import java.util.Optional;

import fr.sictiam.stela.acteservice.model.Acte;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActeRepository extends JpaRepository<Acte, String> {
    Optional<Acte> findByUuid(String uuid);
    Optional<Acte> findByNumero(String numero);
}
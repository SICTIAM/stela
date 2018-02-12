package fr.sictiam.stela.pesservice.dao;

import fr.sictiam.stela.pesservice.model.PesRetour;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PesRetourRepository extends JpaRepository<PesRetour, String> {

    Optional<PesRetour> findByUuid(String uuid);
}
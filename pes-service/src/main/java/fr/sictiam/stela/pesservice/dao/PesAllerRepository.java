package fr.sictiam.stela.pesservice.dao;

import fr.sictiam.stela.pesservice.model.PesAller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PesAllerRepository extends JpaRepository<PesAller, String> {
    Optional<PesAller> findByFileName(String fileName);

    List<PesAller> findByPjFalseAndSignedFalse();
}
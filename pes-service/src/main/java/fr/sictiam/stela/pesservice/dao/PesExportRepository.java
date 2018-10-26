package fr.sictiam.stela.pesservice.dao;

import fr.sictiam.stela.pesservice.model.PesExport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PesExportRepository extends JpaRepository<PesExport, String> {

    Optional<PesExport> findByFileName(String fileName);
}

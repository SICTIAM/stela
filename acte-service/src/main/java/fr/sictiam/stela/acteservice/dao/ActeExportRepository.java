package fr.sictiam.stela.acteservice.dao;

import fr.sictiam.stela.acteservice.model.ActeExport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ActeExportRepository extends JpaRepository<ActeExport, String> {

    Optional<ActeExport> findByFileName(String fileName);
}

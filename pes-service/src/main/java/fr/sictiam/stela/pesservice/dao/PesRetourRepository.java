package fr.sictiam.stela.pesservice.dao;

import fr.sictiam.stela.pesservice.model.PesRetour;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PesRetourRepository extends JpaRepository<PesRetour, String> {

    Optional<PesRetour> findByUuid(String uuid);

    Optional<PesRetour> findByAttachmentFilename(String filename);

    List<PesRetour> findByLocalAuthoritySirenAndCollectedFalse(String siren);

}
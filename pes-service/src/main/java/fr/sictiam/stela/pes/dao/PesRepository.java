package fr.sictiam.stela.pes.dao;

import fr.sictiam.stela.pes.model.Pes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PesRepository extends JpaRepository<Pes, String> {

    Pes findByUuid(String uuid);
}

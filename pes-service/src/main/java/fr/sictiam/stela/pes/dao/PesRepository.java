package fr.sictiam.stela.pes.dao;

import fr.sictiam.stela.pes.model.Pes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PesRepository extends JpaRepository<Pes, String> {

    Iterable<Pes> findAllByOrderByIdAsc();
    Pes findOneByPesId(String pesId);
}

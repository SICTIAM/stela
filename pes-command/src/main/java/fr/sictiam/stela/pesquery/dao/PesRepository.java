package fr.sictiam.stela.pesquery.dao;

import fr.sictiam.stela.pesquery.model.PesEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PesRepository extends JpaRepository<PesEntry, String> {

    Iterable<PesEntry> findAllByOrderByIdAsc();
    PesEntry findOneByPesId(String pesId);
}

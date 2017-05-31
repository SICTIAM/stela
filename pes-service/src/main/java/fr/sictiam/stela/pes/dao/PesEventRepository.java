package fr.sictiam.stela.pes.dao;

import fr.sictiam.stela.pes.model.event.PesEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PesEventRepository extends JpaRepository<PesEvent, String> {

    List<PesEvent> findAllByPesId(String pesId);
}

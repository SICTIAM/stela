package fr.sictiam.stela.pes.dao;

import fr.sictiam.stela.pes.model.PesHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PesHistoryRepository extends JpaRepository<PesHistory, String> {
}

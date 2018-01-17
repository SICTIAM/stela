package fr.sictiam.stela.pesservice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.sictiam.stela.pesservice.model.PesHistory;

import java.util.List;
import java.util.Optional;
import java.util.SortedSet;

public interface PesHistoryRepository extends JpaRepository<PesHistory, String> {
    Optional<PesHistory> findByUuid(String uuid);
    List<PesHistory> findBypesUuidOrderByDate(String acteUuid);
    
}

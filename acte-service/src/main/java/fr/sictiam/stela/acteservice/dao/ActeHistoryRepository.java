package fr.sictiam.stela.acteservice.dao;

import fr.sictiam.stela.acteservice.model.ActeHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.SortedSet;

public interface ActeHistoryRepository extends JpaRepository<ActeHistory, String> {
    Optional<ActeHistory> findByUuid(String uuid);
    List<ActeHistory> findByacteUuidOrderByDate(String acteUuid);
    
}

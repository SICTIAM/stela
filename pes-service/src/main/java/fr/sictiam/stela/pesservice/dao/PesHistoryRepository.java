package fr.sictiam.stela.pesservice.dao;

import fr.sictiam.stela.pesservice.model.PesHistory;
import fr.sictiam.stela.pesservice.model.StatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PesHistoryRepository extends JpaRepository<PesHistory, String> {
    Optional<PesHistory> findByUuid(String uuid);

    List<PesHistory> findBypesUuidOrderByDate(String pesUuid);

    List<PesHistory> findBypesUuidAndStatusInOrderByDateDesc(String pesUuid, List<StatusType> status);

    @Query(nativeQuery = true, value = "SELECT COUNT(1) FROM pes_history WHERE status='SENT' AND date BETWEEN ?1 AND " +
            "?2")
    int countSentToday(LocalDateTime start, LocalDateTime end);
}

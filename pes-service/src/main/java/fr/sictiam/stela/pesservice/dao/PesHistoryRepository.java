package fr.sictiam.stela.pesservice.dao;

import fr.sictiam.stela.pesservice.model.PesHistory;
import fr.sictiam.stela.pesservice.model.StatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PesHistoryRepository extends JpaRepository<PesHistory, String> {
    Optional<PesHistory> findByUuid(String uuid);

    List<PesHistory> findBypesUuidOrderByDate(String pesUuid);

    List<PesHistory> findBypesUuidAndStatusInOrderByDateDesc(String pesUuid, List<StatusType> status);

    @Query(nativeQuery = true, value = "SELECT COUNT(1) FROM pes_history WHERE status='SENT' AND date BETWEEN ?1 AND " +
            "?2")
    int countSentToday(LocalDateTime start, LocalDateTime end);

    Long countByDateBetweenAndStatus(LocalDateTime fromLocalDate, LocalDateTime toLocaleDate, StatusType statusType);

    @Query(nativeQuery = true,
            value = "SELECT date_trunc(:sample, ph.date) AS date_time," +
                    "COUNT(ph)" +
                    "FROM pes_history ph " +
                    "WHERE ph.status = :statusType " +
                    "AND ph.date BETWEEN TO_TIMESTAMP(:fromLocalDate, 'YYYY-mm-DD HH24:MI:SS') AND TO_TIMESTAMP(:toLocalDate, 'YYYY-mm-DD HH24:MI:SS')" +
                    "GROUP BY 1")
    List<Map<LocalDateTime, Long>> countByDateBetweenAndGroupBySample(
            @Param("sample") String sample,
            @Param("statusType") String type,
            @Param("fromLocalDate") LocalDateTime fromLocalDate,
            @Param("toLocalDate") LocalDateTime toLocaleDate);
}

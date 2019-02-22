package fr.sictiam.stela.pesservice.dao;

import fr.sictiam.stela.pesservice.model.ArchiveStatus;
import fr.sictiam.stela.pesservice.model.PesAller;
import fr.sictiam.stela.pesservice.model.StatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PesAllerRepository extends JpaRepository<PesAller, String> {
    Optional<PesAller> findByFileName(String fileName);

    List<PesAller> findAllByLocalAuthorityUuidAndArchiveNull(String uuid);

    List<PesAller.Light> findByPjFalseAndSignedFalseAndLocalAuthoritySesileSubscriptionTrueAndArchiveNull();

    List<PesAller> findAllByLocalAuthorityUuidAndArchive_Status(String uuid, ArchiveStatus archiveStatus);

    List<PesAller> findAllByLocalAuthority_UuidAndLastHistoryStatusAndLastHistoryDateGreaterThan(
            String localAuthorityUuid, StatusType status, LocalDateTime date);

    Long countByLastHistoryStatus(StatusType statusType);

    List<PesAller> findByLastHistoryStatus(StatusType statusType);

    Long countByCreationBetweenAndLastHistoryStatus(LocalDateTime fromLocalDate, LocalDateTime toLocaleDate, StatusType statusType);

    @Query(nativeQuery = true,
        value = "SELECT date_trunc(:sample, pa.creation) AS date_time," +
                "COUNT(pa)" +
                "FROM pes_aller pa " +
                "WHERE pa.last_history_status = :statusType " +
                "AND pa.creation BETWEEN TO_TIMESTAMP(:fromLocalDate, 'YYYY-mm-DD HH24:MI:SS') AND TO_TIMESTAMP(:toLocalDate, 'YYYY-mm-DD HH24:MI:SS')" +
                "GROUP BY 1")
    List<Map<LocalDateTime, Long>> countByCreationBetweenAndGroupByDay(
            @Param("sample") String sample,
            @Param("statusType") String type,
            @Param("fromLocalDate") LocalDateTime fromLocalDate,
            @Param("toLocalDate") LocalDateTime toLocaleDate);

    Long countByLastHistoryStatusAndLastHistoryDateAfter(StatusType statusType, LocalDateTime localDateTime);
}
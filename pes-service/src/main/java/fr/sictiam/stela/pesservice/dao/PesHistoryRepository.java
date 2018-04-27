package fr.sictiam.stela.pesservice.dao;

import fr.sictiam.stela.pesservice.model.PesHistory;
import fr.sictiam.stela.pesservice.model.StatusType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PesHistoryRepository extends JpaRepository<PesHistory, String> {
    Optional<PesHistory> findByUuid(String uuid);

    List<PesHistory> findBypesUuidOrderByDate(String pesUuid);

    List<PesHistory> findBypesUuidAndStatusInOrderByDateDesc(String pesUuid, List<StatusType> status);
}

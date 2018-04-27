package fr.sictiam.stela.convocationservice.dao;

import fr.sictiam.stela.convocationservice.model.ConvocationHistory;
import fr.sictiam.stela.convocationservice.model.StatusType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConvocationHistoryRepository extends JpaRepository<ConvocationHistory, String> {
    Optional<ConvocationHistory> findByUuid(String uuid);

    List<ConvocationHistory> findByconvocationUuidOrderByDate(String pesUuid);

    List<ConvocationHistory> findByconvocationUuidAndStatusInOrderByDateDesc(String pesUuid, List<StatusType> status);
}

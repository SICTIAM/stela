package fr.sictiam.stela.convocationservice.dao;

import fr.sictiam.stela.convocationservice.model.ConvocationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConvocationHistoryRepository extends JpaRepository<ConvocationHistory, String> {
    List<ConvocationHistory> findByConvocationUuidOrderByDate(String uuid);
}

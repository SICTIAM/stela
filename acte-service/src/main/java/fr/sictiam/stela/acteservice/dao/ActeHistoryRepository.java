package fr.sictiam.stela.acteservice.dao;

import fr.sictiam.stela.acteservice.model.ActeHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActeHistoryRepository extends JpaRepository<ActeHistory, String> {

}

package fr.sictiam.stela.acteservice.dao;

import fr.sictiam.stela.acteservice.model.Draft;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ActeDraftRepository extends JpaRepository<Draft, String> {
    Draft findByUuid(String uuid);

    List<Draft> findAllByOrderByLastModifiedDesc();

    List<Draft> findAllByLastModifiedBefore(LocalDateTime date);
}
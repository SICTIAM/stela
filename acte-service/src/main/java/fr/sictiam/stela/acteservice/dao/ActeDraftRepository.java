package fr.sictiam.stela.acteservice.dao;

import fr.sictiam.stela.acteservice.model.ActeDraft;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActeDraftRepository extends JpaRepository<ActeDraft, String> {
    ActeDraft findByUuid(String uuid);
}
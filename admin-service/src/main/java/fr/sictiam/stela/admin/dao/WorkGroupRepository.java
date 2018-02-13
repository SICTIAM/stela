package fr.sictiam.stela.admin.dao;

import fr.sictiam.stela.admin.model.WorkGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkGroupRepository extends JpaRepository<WorkGroup, String> {
    Optional<WorkGroup> findByUuid(String uuid);

    List<WorkGroup> findAllByLocalAuthority_Uuid(String uuid);
}

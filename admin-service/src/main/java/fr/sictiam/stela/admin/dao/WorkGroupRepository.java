package fr.sictiam.stela.admin.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.sictiam.stela.admin.model.Profile;
import fr.sictiam.stela.admin.model.WorkGroup;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WorkGroupRepository extends JpaRepository<WorkGroup, String> {
    Optional<WorkGroup> findByUuid(String uuid);
    List<WorkGroup> findAllByLocalAuthority_Uuid(String uuid);
}

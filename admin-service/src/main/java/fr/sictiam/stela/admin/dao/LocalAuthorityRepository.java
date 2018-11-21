package fr.sictiam.stela.admin.dao;

import fr.sictiam.stela.admin.model.LocalAuthority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface LocalAuthorityRepository extends JpaRepository<LocalAuthority, String> {

    Optional<LocalAuthority> findByUuid(String uuid);

    Optional<LocalAuthority> findByName(String name);

    Optional<LocalAuthority> findBySiren(String siren);

    Optional<LocalAuthority> findBySlugName(String slugName);

    Optional<LocalAuthority> findByOzwilloInstanceInfo_InstanceId(String instanceId);

    @Query("SELECT COUNT (la.uuid) FROM LocalAuthority la")
    Long countAll();

    Page<LocalAuthority> findAllByProfiles_AgentUuidAndProfiles_AdminTrue(String agentUuid, Pageable pageable);

    @Query("SELECT COUNT (la.uuid) FROM LocalAuthority la JOIN Profile p ON p.localAuthority.uuid = la.uuid WHERE p" +
            ".agent.uuid=?1 AND p.admin=true")
    Long countMine(String agentUuid);
}

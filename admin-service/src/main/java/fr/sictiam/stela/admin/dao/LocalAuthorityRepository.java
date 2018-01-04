package fr.sictiam.stela.admin.dao;

import fr.sictiam.stela.admin.model.LocalAuthority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocalAuthorityRepository extends JpaRepository<LocalAuthority, String> {

    Optional<LocalAuthority> findByUuid(String uuid);
    Optional<LocalAuthority> findByName(String name);
    Optional<LocalAuthority> findBySiren(String siren);
    Optional<LocalAuthority> findBySlugName(String slugName);
    Optional<LocalAuthority> findByOzwilloInstanceInfo_InstanceId(String instanceId);
}

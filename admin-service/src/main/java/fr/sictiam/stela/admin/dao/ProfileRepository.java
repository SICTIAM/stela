package fr.sictiam.stela.admin.dao;

import fr.sictiam.stela.admin.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProfileRepository extends JpaRepository<Profile, String> {
    Optional<Profile> findByLocalAuthority_UuidAndAgent_Uuid(String localAuthorityUuid, String agentUuid);

    Optional<Profile> findByUuid(String uuid);

    List<Profile> findByLocalAuthority_Uuid(String localAuthorityUuid);

    Set<Profile> findByAgent_UuidOrderByLocalAuthority_name(String agentUuid);

    Set<Profile> findByAgent_SubOrderByLocalAuthority_name(String sub);

    Optional<Profile> findByLocalAuthority_SirenAndAgent_Email(String siren, String email);
}

package fr.sictiam.stela.admin.dao;

import fr.sictiam.stela.admin.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, String> {
    Optional<Profile> findByLocalAuthority_UuidAndAgent_Uuid(String localAuthorityUuid, String agentUuid);

    Optional<Profile> findByUuid(String uuid);

    List<Profile> findByLocalAuthority_Uuid(String localAuthorityUuid);

    Optional<Profile> findByLocalAuthority_SirenAndAgent_Email(String siren, String email);
}

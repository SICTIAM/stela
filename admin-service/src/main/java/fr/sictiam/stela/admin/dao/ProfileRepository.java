package fr.sictiam.stela.admin.dao;

import fr.sictiam.stela.admin.model.Profile;
import fr.sictiam.stela.admin.model.UI.ProfileSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, String> {
    Optional<Profile> findByLocalAuthority_UuidAndAgent_Uuid(String localAuthorityUuid, String agentUuid);

    Optional<Profile> findByUuid(String uuid);

    List<Profile> findByLocalAuthority_Uuid(String localAuthorityUuid);

    @Transactional(readOnly = true)
    @Query("SELECT p.uuid as uuid, p.localAuthority.name as localAuthorityName FROM Profile p WHERE p.agent.sub = ?1 ORDER BY p.localAuthority.name")
    List<ProfileSummary> fetchProfilesSummaryForAgent(String sub);

    Optional<Profile> findByLocalAuthority_SirenAndAgent_Email(String siren, String email);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Profile p WHERE p.agent.uuid=?1 AND p" +
            ".localAuthority.uuid=?2 AND p.admin=true")
    boolean isAdmin(String agentUuid, String localAuthorityUuid);
}

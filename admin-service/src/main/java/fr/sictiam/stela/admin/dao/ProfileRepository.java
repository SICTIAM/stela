package fr.sictiam.stela.admin.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.sictiam.stela.admin.model.Profile;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, String> {
    Optional<Profile> findByLocalAuthority_UuidAndAgent_Uuid(String localAuthorityUuid, String agentUuid);
    Optional<Profile> findByUuid(String uuid);
}

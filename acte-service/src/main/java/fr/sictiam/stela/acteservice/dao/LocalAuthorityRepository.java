package fr.sictiam.stela.acteservice.dao;

import fr.sictiam.stela.acteservice.model.LocalAuthority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocalAuthorityRepository extends JpaRepository<LocalAuthority, String> {
    LocalAuthority findByUuid(String uuid);

    Optional<LocalAuthority> findByName(String name);
}

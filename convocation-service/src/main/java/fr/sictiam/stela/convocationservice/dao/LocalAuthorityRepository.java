package fr.sictiam.stela.convocationservice.dao;

import fr.sictiam.stela.convocationservice.model.LocalAuthority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LocalAuthorityRepository extends JpaRepository<LocalAuthority, String> {
    Optional<LocalAuthority> findByUuid(String uuid);

    Optional<LocalAuthority> findByName(String name);

    Optional<LocalAuthority> findBySiren(String siren);

    List<LocalAuthority> findAllByActiveTrue();
}

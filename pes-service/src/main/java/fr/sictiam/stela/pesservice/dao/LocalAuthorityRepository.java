package fr.sictiam.stela.pesservice.dao;

import fr.sictiam.stela.pesservice.model.LocalAuthority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LocalAuthorityRepository extends JpaRepository<LocalAuthority, String> {
    Optional<LocalAuthority> findByUuid(String uuid);

    Optional<LocalAuthority> findByName(String name);

    Optional<LocalAuthority> findBySiren(String siren);

    List<LocalAuthority> findAllByActiveTrue();

    Optional<LocalAuthority> findByActiveTrueAndSirenEqualsOrSirens(String siren1, String siren2);

    List<LocalAuthority> findByActiveTrueAndSirens(String siren);

    List<LocalAuthority> findAllByActiveTrueOrderByName();
}

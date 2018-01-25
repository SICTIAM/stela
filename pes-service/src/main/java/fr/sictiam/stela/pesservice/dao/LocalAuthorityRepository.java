package fr.sictiam.stela.pesservice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.sictiam.stela.pesservice.model.LocalAuthority;

import java.util.Optional;

public interface LocalAuthorityRepository extends JpaRepository<LocalAuthority, String> {
    Optional<LocalAuthority> findByUuid(String uuid);

    Optional<LocalAuthority> findByName(String name);

    Optional<LocalAuthority> findBySiren(String siren);

    Optional<LocalAuthority> findBySiret(String siret);
}

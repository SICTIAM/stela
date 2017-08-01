package fr.sictiam.stela.admin.dao;

import fr.sictiam.stela.admin.model.LocalAuthority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LocalAuthorityRepository extends JpaRepository<LocalAuthority, String> {

    List<LocalAuthority> findAll();
    Optional<LocalAuthority> findByName(String name);
    Optional<LocalAuthority> findBySiren(String siren);
}

package fr.sictiam.stela.acteservice.dao;

import fr.sictiam.stela.acteservice.model.LocalAuthority;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LocalAuthorityRepository extends JpaRepository<LocalAuthority, String> {
    Optional<LocalAuthority> findByUuid(String uuid);

    Optional<LocalAuthority> findByName(String name);

    @EntityGraph(attributePaths = { "materialCodes" })
    Optional<LocalAuthority> findWithMaterialCodesBySiren(String siren);

    Optional<LocalAuthority> findBySiren(String siren);

    List<LocalAuthority> findAllByActiveTrueOrderByName();
}

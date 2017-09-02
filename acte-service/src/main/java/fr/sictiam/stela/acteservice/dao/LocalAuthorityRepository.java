package fr.sictiam.stela.acteservice.dao;

import fr.sictiam.stela.acteservice.model.LocalAuthority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocalAuthorityRepository extends JpaRepository<LocalAuthority, String> {
    LocalAuthority findByUuid(String uuid);
}

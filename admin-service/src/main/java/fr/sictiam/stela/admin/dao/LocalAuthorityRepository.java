package fr.sictiam.stela.admin.dao;

import fr.sictiam.stela.admin.model.LocalAuthority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocalAuthorityRepository extends JpaRepository<LocalAuthority, String> {
    LocalAuthority findByUuid(String uuid);
}

package fr.sictiam.stela.acteservice.dao;

import fr.sictiam.stela.acteservice.model.MaterialCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MaterialCodeRepository extends JpaRepository<MaterialCode, String> {
    Optional<MaterialCode> findByCodeAndLocalAuthorityUuid(String code, String uuid);
}

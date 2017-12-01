package fr.sictiam.stela.acteservice.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.sictiam.stela.acteservice.model.LocalAuthority;
import fr.sictiam.stela.acteservice.model.MaterialCode;

public interface MaterialCodeRepository extends JpaRepository<MaterialCode, String> {
    Optional<MaterialCode> findByCodeAndLocalAuthorityUuid(String code, String uuid);
}

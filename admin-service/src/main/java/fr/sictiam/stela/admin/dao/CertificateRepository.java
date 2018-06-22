package fr.sictiam.stela.admin.dao;

import fr.sictiam.stela.admin.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, String> {
    Optional<Certificate> findByUuid(String uuid);
    Optional<Certificate> findBySerialAndIssuer(String serial, String issuer);
}

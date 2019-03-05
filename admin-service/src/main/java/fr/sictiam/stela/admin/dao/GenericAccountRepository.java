package fr.sictiam.stela.admin.dao;

import fr.sictiam.stela.admin.model.GenericAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GenericAccountRepository extends JpaRepository<GenericAccount, String> {

    Optional<GenericAccount> findByEmailIgnoreCase(String email);

    Optional<GenericAccount> findBySerialAndVendor(String serial, String vendor);
}

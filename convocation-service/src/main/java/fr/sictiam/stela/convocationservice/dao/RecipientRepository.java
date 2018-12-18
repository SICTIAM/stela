package fr.sictiam.stela.convocationservice.dao;

import fr.sictiam.stela.convocationservice.model.Recipient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecipientRepository extends JpaRepository<Recipient, String> {

    public Optional<Recipient> findByUuid(String uuid);

    public Optional<Recipient> findByEmailAndLocalAuthorityUuid(String email, String localAuthorityUuid);

    public List<Recipient> findAllByLocalAuthorityUuid(String localAuthorityUuid);

    public Optional<Recipient> findByToken(String token);

}

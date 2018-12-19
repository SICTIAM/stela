package fr.sictiam.stela.convocationservice.dao;

import fr.sictiam.stela.convocationservice.model.Recipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RecipientRepository extends JpaRepository<Recipient, String> {

    public Optional<Recipient> findByUuid(String uuid);

    public Optional<Recipient> findByEmailAndLocalAuthorityUuid(String email, String localAuthorityUuid);

    public List<Recipient> findAllByLocalAuthorityUuid(String localAuthorityUuid);

    @Query(nativeQuery = true, value = "SELECT COUNT(1) FROM recipient WHERE uuid!=:uuid AND " +
            "local_authority_uuid=:localAuthorityUuid AND email=:email")
    public Long recipientExists(String uuid, String localAuthorityUuid, String email);

    public Optional<Recipient> findByToken(String token);

}

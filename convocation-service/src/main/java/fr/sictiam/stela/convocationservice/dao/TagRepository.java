package fr.sictiam.stela.convocationservice.dao;

import fr.sictiam.stela.convocationservice.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, String> {

    List<Tag> findTagsByLocalAuthorityUuidOrderByName(String localAuthorityUuid);

    Optional<Tag> findByUuidAndLocalAuthorityUuid(String uuid, String localAuthorityUuid);

    Optional<Tag> findByUuid(String uuid);
}

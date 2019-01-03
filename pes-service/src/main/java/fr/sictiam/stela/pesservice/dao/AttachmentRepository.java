package fr.sictiam.stela.pesservice.dao;

import fr.sictiam.stela.pesservice.model.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, String> {
    Optional<Attachment> findByUuid(String uuid);

    @Modifying
    @Transactional
    @Query(value = "UPDATE attachment SET file=:content WHERE uuid=:uuid", nativeQuery = true)
    int updateContent(@Param("uuid") String uuid, @Param("content") byte[] content);
}

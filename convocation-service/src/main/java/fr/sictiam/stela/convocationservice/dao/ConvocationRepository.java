package fr.sictiam.stela.convocationservice.dao;

import fr.sictiam.stela.convocationservice.model.Convocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ConvocationRepository extends JpaRepository<Convocation, String> {

    public Optional<Convocation> findByUuidAndLocalAuthorityUuid(String uuid, String localAuthorityUuid);

    @Modifying
    @Transactional
    @Query(value = "UPDATE Convocation c SET c.sentDate=:now where uuid=:uuid")
    public void setSentDate(@Param("uuid") String uuid, @Param("now") LocalDateTime now);

    public List<Convocation> findByCancelledFalseAndMeetingDateBetween(LocalDateTime start, LocalDateTime end);

    @Query(
            nativeQuery = true,
            value = "select uuid " +
                    "from (" +
                    "    select uuid, creation_date, meeting_date, round((meeting_date::::timestamp::::date - " +
                    "creation_date::::timestamp::::date)/2) as diff from convocation where cancelled=false) as tmp " +
                    "where creation_date::::timestamp::::date + integer '1' * diff::::integer = current_date;"
    )
    public List<String> findByHalfDuration();
}

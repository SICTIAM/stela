package fr.sictiam.stela.convocationservice.dao;

import fr.sictiam.stela.convocationservice.model.Convocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConvocationRepository extends JpaRepository<Convocation, String> {

    public Optional<Convocation> findByUuidAndLocalAuthorityUuid(String uuid, String localAuthorityUuid);

}

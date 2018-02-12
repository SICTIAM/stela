package fr.sictiam.stela.pesservice.dao;

import fr.sictiam.stela.pesservice.model.PesRetour;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PesRetourRepository extends JpaRepository<PesRetour, String> {

    Optional<PesRetour> findByUuid(String uuid);

    Page<PesRetour> findAllByLocalAuthority_Uuid(String uuid, Pageable pageable);

    Long countAllByLocalAuthority_Uuid(String uuid);
}
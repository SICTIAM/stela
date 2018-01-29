package fr.sictiam.stela.pesservice.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.sictiam.stela.pesservice.model.PesAller;
import fr.sictiam.stela.pesservice.model.StatusType;

public interface PesAllerRepository extends JpaRepository<PesAller, String> {
    Optional<PesAller> findByAttachment_filename(String fileName);
    List<PesAller> findAllByPesHistories_statusAndPesHistories_statusNotIn(StatusType status, List<StatusType> notStatus);
   
}
package fr.sictiam.stela.pesservice.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.sictiam.stela.pesservice.model.PesAller;

public interface PesAllerRepository extends JpaRepository<PesAller, String> {

    Optional<PesAller> findByAttachment_filename(String fileName);
   
}
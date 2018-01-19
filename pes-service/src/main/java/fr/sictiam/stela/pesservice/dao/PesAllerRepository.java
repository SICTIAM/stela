package fr.sictiam.stela.pesservice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.sictiam.stela.pesservice.model.PesAller;

public interface PesAllerRepository extends JpaRepository<PesAller, String> {
   
}
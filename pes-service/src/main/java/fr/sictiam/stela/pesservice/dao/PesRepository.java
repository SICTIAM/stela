package fr.sictiam.stela.pesservice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.sictiam.stela.pesservice.model.Pes;

public interface PesRepository extends JpaRepository<Pes, String> {
   
}
package fr.sictiam.stela.acteservice.dao;

import fr.sictiam.stela.acteservice.model.Acte;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActeRepository extends JpaRepository<Acte, Long> {

    Acte findById(Long id);
    Acte findByNumero(String numero);
}
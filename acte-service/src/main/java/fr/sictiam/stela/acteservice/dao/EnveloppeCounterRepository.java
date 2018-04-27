package fr.sictiam.stela.acteservice.dao;

import fr.sictiam.stela.acteservice.model.EnveloppeCounter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface EnveloppeCounterRepository extends JpaRepository<EnveloppeCounter, String> {

    Optional<EnveloppeCounter> findByDate(LocalDate date);
}

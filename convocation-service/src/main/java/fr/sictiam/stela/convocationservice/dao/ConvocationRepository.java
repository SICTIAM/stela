package fr.sictiam.stela.convocationservice.dao;

import fr.sictiam.stela.convocationservice.model.Convocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConvocationRepository extends JpaRepository<Convocation, String> {

}

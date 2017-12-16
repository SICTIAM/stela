package fr.sictiam.stela.acteservice.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.sictiam.stela.acteservice.model.Agent;

public interface AgentRepository extends JpaRepository<Agent, String> {
    Optional<Agent> findBySub(String sub);
}

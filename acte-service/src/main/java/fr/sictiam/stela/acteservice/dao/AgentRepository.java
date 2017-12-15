package fr.sictiam.stela.acteservice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.sictiam.stela.acteservice.model.Agent;

public interface AgentRepository extends JpaRepository<Agent, String> {

}

package fr.sictiam.stela.admin.dao;

import fr.sictiam.stela.admin.model.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentRepository extends JpaRepository<Agent, String> {
}

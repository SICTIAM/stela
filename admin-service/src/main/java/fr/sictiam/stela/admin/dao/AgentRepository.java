package fr.sictiam.stela.admin.dao;

import fr.sictiam.stela.admin.model.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AgentRepository extends JpaRepository<Agent, String> {

    Optional<Agent> findBySub(String sub);
}

package fr.sictiam.stela.admin.dao;

import fr.sictiam.stela.admin.model.AgentConnection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentConnectionRepository extends JpaRepository<AgentConnection, String> {
}

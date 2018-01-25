package fr.sictiam.stela.admin.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.sictiam.stela.admin.model.AgentConnection;

public interface AgentConnectionRepository extends JpaRepository<AgentConnection, String> {
}

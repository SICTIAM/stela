package fr.sictiam.stela.admin.dao;

import fr.sictiam.stela.admin.model.Agent;
import fr.sictiam.stela.admin.model.AgentModule;
import fr.sictiam.stela.admin.model.LocalAuthority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AgentModuleRepository extends JpaRepository<AgentModule, AgentModule.AgentModuleId> {

    AgentModule findByAgent(Agent agent);
    Optional<AgentModule> findByAgentAndLocalAuthority(Agent agent, LocalAuthority localAuthority);
}

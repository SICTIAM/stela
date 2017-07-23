package fr.sictiam.stela.admin.service;

import fr.sictiam.stela.admin.dao.AgentModuleRepository;
import fr.sictiam.stela.admin.model.Agent;
import fr.sictiam.stela.admin.model.AgentModule;
import org.springframework.stereotype.Service;

@Service
public class AgentModuleService {

    private final AgentModuleRepository agentModuleRepository;

    public AgentModuleService(AgentModuleRepository agentModuleRepository) {
        this.agentModuleRepository = agentModuleRepository;
    }

    AgentModule findByAgent(Agent agent) {
        return agentModuleRepository.findByAgent(agent);
    }
}

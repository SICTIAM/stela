package fr.sictiam.stela.admin.service;

import org.springframework.stereotype.Service;

import fr.sictiam.stela.admin.dao.AgentConnectionRepository;
import fr.sictiam.stela.admin.model.AgentConnection;

@Service
public class AgentConnectionService {

    private final AgentConnectionRepository agentConnectionRepository;

    public AgentConnectionService(AgentConnectionRepository agentConnectionRepository) {
        this.agentConnectionRepository = agentConnectionRepository;
    }

    public AgentConnection save(AgentConnection agentConnection) {
        return agentConnectionRepository.save(agentConnection);
    }

}

package fr.sictiam.stela.admin.service;

import fr.sictiam.stela.admin.dao.AgentRepository;
import fr.sictiam.stela.admin.dao.LocalAuthorityRepository;
import fr.sictiam.stela.admin.model.Agent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AgentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentService.class);

    private final AgentRepository agentRepository;
    private final LocalAuthorityRepository localAuthorityRepository;

    public AgentService(AgentRepository agentRepository, LocalAuthorityRepository localAuthorityRepository) {
        this.agentRepository = agentRepository;
        this.localAuthorityRepository = localAuthorityRepository;
    }

    public Agent createIfNotExists(Agent agent) {
        return agentRepository.findBySub(agent.getSub()).orElseGet(() -> create(agent));
    }

    public Agent create(Agent agent) {
        Agent savedAgent = agentRepository.save(agent);
        if (agent.isAdmin()) {
            // if agent is an admin, give it access to all activated modules for current local authority
        }

        return savedAgent;
    }
}

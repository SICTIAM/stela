package fr.sictiam.stela.acteservice.service;

import java.util.Optional;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fr.sictiam.stela.acteservice.dao.AgentRepository;
import fr.sictiam.stela.acteservice.model.Agent;

@Service
public class AgentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentService.class);

    private final AgentRepository agentRepository;

    public AgentService(AgentRepository agentRepository) {
        this.agentRepository = agentRepository;
    }

    public Optional<Agent> findBySub(String sub) {
        return agentRepository.findBySub(sub);
    }

}

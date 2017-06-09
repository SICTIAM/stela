package fr.sictiam.stela.admin.service;

import fr.sictiam.stela.admin.dao.AgentModuleRepository;
import fr.sictiam.stela.admin.dao.AgentRepository;
import fr.sictiam.stela.admin.dao.LocalAuthorityRepository;
import fr.sictiam.stela.admin.model.Agent;
import fr.sictiam.stela.admin.model.AgentModule;
import fr.sictiam.stela.admin.model.LocalAuthority;
import fr.sictiam.stela.admin.model.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AgentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentService.class);

    private final AgentRepository agentRepository;
    private final LocalAuthorityRepository localAuthorityRepository;
    private final AgentModuleRepository agentModuleRepository;

    public AgentService(AgentRepository agentRepository, LocalAuthorityRepository localAuthorityRepository,
                        AgentModuleRepository agentModuleRepository) {
        this.agentRepository = agentRepository;
        this.localAuthorityRepository = localAuthorityRepository;
        this.agentModuleRepository = agentModuleRepository;
    }

    public Agent createIfNotExists(Agent agent) {
        return agentRepository.findBySub(agent.getSub()).orElseGet(() -> create(agent));
    }

    public Agent create(Agent agent) {
        Agent savedAgent = agentRepository.save(agent);
        if (agent.isAdmin()) {
            // if agent is an admin, give it access to all activated modules for current local authority

            // TODO : local authority management, for now, we take the first and unique one
            LocalAuthority localAuthority = localAuthorityRepository.findAll().get(0);
            setModules(savedAgent.getUuid(), localAuthority.getUuid(), localAuthority.getActivatedModules());
        }

        return savedAgent;
    }

    public void setModules(String agentUuid, String localAuthorityUuid, Set<Module> modules) {
        AgentModule agentModule = new AgentModule(localAuthorityRepository.findOne(localAuthorityUuid),
                agentRepository.findOne(agentUuid),
                modules);
        agentModuleRepository.save(agentModule);
    }

    public void addModule(String agentUuid, String localAuthorityUuid, Module module) {
        AgentModule agentModule = new AgentModule(
                localAuthorityRepository.findOne(localAuthorityUuid),
                agentRepository.findOne(agentUuid),
                module
        );
        agentModuleRepository.save(agentModule);
    }
}

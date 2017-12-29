package fr.sictiam.stela.admin.service;

import java.util.Optional;

import fr.sictiam.stela.admin.service.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fr.sictiam.stela.admin.dao.AgentRepository;
import fr.sictiam.stela.admin.dao.ProfileRepository;
import fr.sictiam.stela.admin.model.Agent;
import fr.sictiam.stela.admin.model.LocalAuthority;
import fr.sictiam.stela.admin.model.Profile;

@Service
public class AgentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentService.class);

    private final AgentRepository agentRepository;
    private final ProfileRepository profileRepository;
    private final LocalAuthorityService localAuthorityService;

    public AgentService(AgentRepository agentRepository, ProfileRepository profileRepository,
            LocalAuthorityService localAuthorityService) {
        this.agentRepository = agentRepository;
        this.profileRepository = profileRepository;
        this.localAuthorityService = localAuthorityService;
    }

    private Agent createIfNotExists(Agent agent) {
        return agentRepository.findBySub(agent.getSub()).orElseGet(() -> agentRepository.save(agent));
    }

    public Optional<Agent> findBySub(String sub) {
        return agentRepository.findBySub(sub);
    }

    public Agent createAndAttach(Agent agent) {
        final String slugName = agent.getSlugName();
        LocalAuthority localAuthority =
                localAuthorityService.getBySlugName(slugName)
                        .orElseThrow(() -> new NotFoundException("No local authority found for slug " + slugName));
        agent = createIfNotExists(agent);
        Profile profile = new Profile(localAuthority, agent, agent.isAdmin());
        profileRepository.save(profile);
        agent.getProfiles().add(profile);
        agent = agentRepository.save(agent);
        localAuthority.getProfiles().add(profile);
        localAuthorityService.createOrUpdate(localAuthority);

        return agent;
    }
}

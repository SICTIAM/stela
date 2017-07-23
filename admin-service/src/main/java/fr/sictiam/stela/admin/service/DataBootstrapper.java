package fr.sictiam.stela.admin.service;

import fr.sictiam.stela.admin.model.Agent;
import fr.sictiam.stela.admin.model.LocalAuthority;
import fr.sictiam.stela.admin.model.Module;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("bootstrap-data")
public class DataBootstrapper implements CommandLineRunner {

    private final LocalAuthorityService localAuthorityService;

    private final AgentService agentService;

    public DataBootstrapper(LocalAuthorityService localAuthorityService, AgentService agentService) {
        this.localAuthorityService = localAuthorityService;
        this.agentService = agentService;
    }

    @Override
    public void run(String... args) throws Exception {
        LocalAuthority localAuthority = new LocalAuthority("Mouans Sartoux", "123456789");
        localAuthority = localAuthorityService.create(localAuthority);
        localAuthorityService.addModule(localAuthority.getUuid(), Module.PES);

        Agent agent = new Agent("Dupont", "Jean", "dev+stela3@sictiam.fr");
        agent = agentService.create(agent);
        agentService.addModule(agent.getUuid(), localAuthority.getUuid(), Module.PES);
    }
}

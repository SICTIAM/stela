package fr.sictiam.stela.admin.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.sictiam.stela.admin.model.Agent;
import fr.sictiam.stela.admin.model.LocalAuthority;
import fr.sictiam.stela.admin.service.AgentService;
import fr.sictiam.stela.admin.service.LocalAuthorityService;

@RestController
@RequestMapping("/api/admin/agent")
public class AgentController {

    private final AgentService agentService;

    private final LocalAuthorityService localAuthorityService;

    public AgentController(AgentService agentService, LocalAuthorityService localAuthorityService) {
        this.agentService = agentService;
        this.localAuthorityService = localAuthorityService;
    }

    @PostMapping
    public Agent createCurrentUser(@RequestBody Agent agent) {
        //add to bootstraped local auth
        LocalAuthority localAuthority = localAuthorityService.getByUuid("639fd48c-93b9-4569-a414-3b372c71e0a1");

        return agentService.createAndAttach(agent, localAuthority);
    }
}

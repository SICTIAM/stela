package fr.sictiam.stela.admin.controller;

import fr.sictiam.stela.admin.model.Agent;
import fr.sictiam.stela.admin.service.AgentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/agent")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping
    public Agent createIfNotExists(@RequestBody Agent agent) {
        return agentService.createIfNotExists(agent);
    }
}

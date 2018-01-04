package fr.sictiam.stela.admin.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import fr.sictiam.stela.admin.model.Agent;
import fr.sictiam.stela.admin.model.UI.Views;
import fr.sictiam.stela.admin.model.Profile;
import fr.sictiam.stela.admin.service.AgentService;

@RestController
@RequestMapping("/api/admin/agent")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping
    @JsonView(Views.ProfileView.class)
    public Profile createCurrentUser(@RequestBody Agent agent) {
        return agentService.createAndAttach(agent);
    }
    
    @GetMapping
    @JsonView(Views.AgentView.class)
    public Agent getCurrent(@RequestHeader("STELA-Sub") String sub) {
        return agentService.findBySub(sub).get();
    }
}

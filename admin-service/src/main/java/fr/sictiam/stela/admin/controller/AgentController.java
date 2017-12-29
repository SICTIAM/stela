package fr.sictiam.stela.admin.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import fr.sictiam.stela.admin.model.Agent;
import fr.sictiam.stela.admin.model.UI.Views;
import fr.sictiam.stela.admin.service.AgentService;

@RestController
@RequestMapping("/api/admin/agent")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }
  
    @GetMapping
    @JsonView(Views.AgentViewPublic.class)
    public Agent getCurrent(@RequestHeader("sub") String sub) {
        return agentService.findBySub(sub).get();
    }
}

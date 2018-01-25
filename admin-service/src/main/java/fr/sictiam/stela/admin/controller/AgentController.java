package fr.sictiam.stela.admin.controller;

import java.util.Date;
import java.util.List;

import fr.sictiam.stela.admin.model.UI.AgentResultsUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.sictiam.stela.admin.model.Agent;
import fr.sictiam.stela.admin.model.AgentConnection;
import fr.sictiam.stela.admin.model.Profile;
import fr.sictiam.stela.admin.model.UI.Views;
import fr.sictiam.stela.admin.service.AgentConnectionService;
import fr.sictiam.stela.admin.service.AgentService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@RestController
@RequestMapping("/api/admin/agent")
public class AgentController {

    private final AgentService agentService;
    
    private final AgentConnectionService agentConnectionService;

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentController.class);
    
    @Value("${application.jwt.expire}")
    long EXPIRATIONTIME; 
    
    @Value("${application.jwt.secret}")
    String SECRET;

    public AgentController(AgentService agentService, AgentConnectionService agentConnectionService) {
        this.agentService = agentService;
        this.agentConnectionService = agentConnectionService;
    }

    @PostMapping
    @JsonView(Views.ProfileView.class)
    public String createCurrentUser(@RequestBody Agent agent) {

        Profile profile = agentService.createAndAttach(agent);
        agentConnectionService.save(new AgentConnection(profile));
        try {
            ObjectMapper mapper = new ObjectMapper();
            String body = mapper.writerWithView(Views.ProfileView.class).writeValueAsString(profile);
            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            
            String jwtToken = Jwts.builder().setSubject(body)
                    .setExpiration(new Date(System.currentTimeMillis() + EXPIRATIONTIME))
                    .signWith(SignatureAlgorithm.HS512, SECRET).compact();
            
            return jwtToken;
            
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage());
        } 

        return null;
    }

    @GetMapping
    @JsonView(Views.AgentView.class)
    public Agent getCurrent(@RequestAttribute("STELA-Sub") String sub) {
        return agentService.findBySub(sub).get();
    }

    @GetMapping("/all")
    @JsonView(Views.AgentViewPublic.class)
    public AgentResultsUI getAllAgent(
            @RequestParam(value = "search", required = false, defaultValue = "") String search,
            @RequestParam(value = "localAuthorityUuid", required = false, defaultValue = "") String localAuthorityUuid,
            @RequestParam(value = "limit", required = false, defaultValue = "25") Integer limit,
            @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
            @RequestParam(value = "column", required = false, defaultValue = "familyName") String column,
            @RequestParam(value = "direction", required = false, defaultValue = "ASC") String direction) {
        List<Agent> localAuthorities = agentService.getAllWithPagination(search, localAuthorityUuid, limit, offset, column, direction);
        Long count = agentService.countAll();
        return new AgentResultsUI(count, localAuthorities);
    }
}

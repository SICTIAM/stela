package fr.sictiam.stela.admin.filter;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import fr.sictiam.stela.admin.model.Agent;
import fr.sictiam.stela.admin.service.AgentService;

@Component
public class Authfilter extends OncePerRequestFilter {

    @Autowired
    AgentService agentService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String sub = request.getHeader("sub");
        Optional<Agent> currentAgent = agentService.findBySub(sub);
        if(currentAgent.isPresent()) {
            request.setAttribute("CurrentProfile", currentAgent.get().getProfiles().stream().findFirst().get().getUuid());
            request.setAttribute("STELA-Current-Local-Authority-UUID", currentAgent.get().getProfiles().stream().findFirst().get().getLocalAuthority().getUuid());
        }
            
        filterChain.doFilter(request, response);
    }
}

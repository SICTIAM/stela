package fr.sictiam.stela.acteservice.filter;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import fr.sictiam.stela.acteservice.dao.ProfileRepository;
import fr.sictiam.stela.acteservice.model.Agent;
import fr.sictiam.stela.acteservice.model.Profile;
import fr.sictiam.stela.acteservice.service.AgentService;

@Component
public class AuthFilter extends OncePerRequestFilter {

    @Autowired
    AgentService agentService;
    
    @Autowired 
    ProfileRepository profileRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String sub = request.getHeader("STELA-Sub");
        String activeProfile = request.getHeader("STELA-Active-Profile");
        
        Profile profile = null;
        
        if (StringUtils.isNotBlank(activeProfile)) {
            profile = profileRepository.findById(activeProfile).get();
        } else {
            Optional<Agent> currentAgent = agentService.findBySub(sub);
            if (currentAgent.isPresent()) {
                profile = currentAgent.get().getProfiles().stream().findFirst().get();
            }
        }
        if (profile != null) {
            request.setAttribute("STELA-Current-Profile", profile.getUuid());
            request.setAttribute("STELA-Current-Local-Authority-UUID", profile.getLocalAuthority().getUuid());
        }
            
        filterChain.doFilter(request, response);    
    }
}

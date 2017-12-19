package fr.sictiam.stela.admin.filter;

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

import fr.sictiam.stela.admin.model.Agent;
import fr.sictiam.stela.admin.model.Profile;
import fr.sictiam.stela.admin.service.AgentService;
import fr.sictiam.stela.admin.service.ProfileService;

@Component
public class Authfilter extends OncePerRequestFilter {

    @Autowired
    AgentService agentService;
    
    @Autowired
    ProfileService profileService;
   
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String sub = request.getHeader("sub");
        String activeProfile = request.getHeader("activeProfile");
        
        Profile profile = null;
        
        if(StringUtils.isNotBlank(activeProfile)) {
            profile =profileService.getByUuid(activeProfile);
        }else {
            Optional<Agent> currentAgent = agentService.findBySub(sub);
            if(currentAgent.isPresent()) {
                profile = currentAgent.get().getProfiles().stream().findFirst().get();
            }
        }
        if(profile != null) {
            request.setAttribute("CurrentProfile", profile.getUuid());
            request.setAttribute("STELA-Current-Local-Authority-UUID", profile.getLocalAuthority().getUuid());
        }
            
        filterChain.doFilter(request, response);
    }
}

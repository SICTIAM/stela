package fr.sictiam.stela.acteservice.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;

@Component
public class AuthFilter extends OncePerRequestFilter {

    @Value("${application.jwt.secret}")
    String SECRET;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String profile = null;
        String currentLocalAuthority = null;
        
        JsonNode token =getToken(request);
           
        if (token != null && StringUtils.isNotBlank(token.get("uuid").asText())) {
            profile = token.get("uuid").asText();
            currentLocalAuthority = token.get("localAuthority").get("uuid").asText();
        } 
        if (profile != null) {
            request.setAttribute("STELA-Current-Profile", profile);
            request.setAttribute("STELA-Current-Local-Authority-UUID", currentLocalAuthority);
        }
            
        filterChain.doFilter(request, response);    
    }
    
    JsonNode getToken(HttpServletRequest request) throws IOException {
        String token = request.getHeader("STELA-Active-Token");
        if (token != null) {
            // parse the token.
            String tokenParsed = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody().getSubject();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode node = objectMapper.readTree(tokenParsed);

            return node;

        }
        return null;
    }
}

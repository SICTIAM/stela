package fr.sictiam.stela.admin.config.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sictiam.stela.admin.model.Certificate;
import fr.sictiam.stela.admin.model.Profile;
import fr.sictiam.stela.admin.service.ProfileService;
import fr.sictiam.stela.admin.service.util.CertUtilService;
import io.jsonwebtoken.Jwts;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

@Component
public class AuthFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthFilter.class);

    @Value("${application.jwt.secret}")
    String SECRET;

    private final CertUtilService certUtilService;
    private final ProfileService profileService;

    public AuthFilter(CertUtilService certUtilService, ProfileService profileService) {
        this.certUtilService = certUtilService;
        this.profileService = profileService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Certificate certificate = certUtilService.getCertInfosFromHeaders(request);
        JsonNode token = getToken(request);

        Profile profile = null;
        if (token != null && StringUtils.isNotBlank(token.get("uuid").asText())) {
            profile = profileService.getByUuid(token.get("uuid").asText());
        }

        if (profile != null) {
            ObjectMapper om = new ObjectMapper();
            Certificate pairedCertificate =
                    token.get("agent").get("certificate") != null && !token.get("agent").get("certificate").isNull()
                            ? om.treeToValue(token.get("agent").get("certificate"), Certificate.class) : null;

            request.setAttribute("STELA-Current-Profile-Is-Local-Authority-Admin", token.get("admin").asBoolean());
            request.setAttribute("STELA-Current-Profile-UUID", profile.getUuid());
            request.setAttribute("STELA-Sub", profile.getAgent().getSub());
            request.setAttribute("STELA-Current-Agent-UUID", token.get("agent").get("uuid").asText());
            request.setAttribute("STELA-Current-Local-Authority-UUID", profile.getLocalAuthority().getUuid());
            request.setAttribute("STELA-Current-Profile-Paired-Certificate", pairedCertificate);
            request.setAttribute("STELA-Certificate", certificate);
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

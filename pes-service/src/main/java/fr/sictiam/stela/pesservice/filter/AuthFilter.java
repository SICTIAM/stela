package fr.sictiam.stela.pesservice.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sictiam.stela.pesservice.model.Right;
import fr.sictiam.stela.pesservice.model.util.Certificate;
import fr.sictiam.stela.pesservice.service.util.CertUtilService;
import io.jsonwebtoken.Jwts;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Component
public class AuthFilter extends OncePerRequestFilter {

    @Value("${application.jwt.secret}")
    String SECRET;

    private final CertUtilService certUtilService;

    public AuthFilter(CertUtilService certUtilService) {
        this.certUtilService = certUtilService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Certificate certificate = certUtilService.getCertInfosFromHeaders(request);
        JsonNode token = getToken(request);

        if (token != null && StringUtils.isNotBlank(token.get("uuid").asText())) {
            ObjectMapper om = new ObjectMapper();
            Certificate pairedCertificate =
                    token.get("agent").get("certificate") != null && !token.get("agent").get("certificate").isNull()
                            ? om.treeToValue(token.get("agent").get("certificate"), Certificate.class) : null;

            Set<Right> rights = new HashSet<>();
            token.get("groups").forEach(group -> group.get("rights").forEach(right -> {
                if (StringUtils.startsWith(right.asText(), "PES_")) {
                    rights.add(Right.valueOf(right.asText()));
                }
            }));
            request.setAttribute("STELA-Current-Profile-Is-Local-Authority-Admin", token.get("admin").asBoolean());
            request.setAttribute("STELA-Current-Profile-Rights", rights);
            request.setAttribute("STELA-Current-Profile-UUID", token.get("uuid").asText());
            request.setAttribute("STELA-Current-Profile-Paired-Certificate", pairedCertificate);
            request.setAttribute("STELA-Current-Local-Authority-UUID",
                    token.get("localAuthority").get("uuid").asText());
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

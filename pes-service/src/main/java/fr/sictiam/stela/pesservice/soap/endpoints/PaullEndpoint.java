package fr.sictiam.stela.pesservice.soap.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sictiam.stela.pesservice.model.ui.GenericAccount;
import fr.sictiam.stela.pesservice.service.ExternalRestService;
import fr.sictiam.stela.pesservice.service.LocalAuthorityService;
import fr.sictiam.stela.pesservice.service.PesAllerService;
import fr.sictiam.stela.pesservice.service.PesRetourService;
import fr.sictiam.stela.pesservice.soap.model.ObjectFactory;
import fr.sictiam.stela.pesservice.soap.model.paull.DepotPESAllerRequest;
import fr.sictiam.stela.pesservice.soap.model.paull.DepotPESAllerResponse;
import fr.sictiam.stela.pesservice.soap.model.paull.PaullSoapToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.io.IOException;
import java.util.Date;

@Endpoint
public class PaullEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaullEndpoint.class);

    private static final String NAMESPACE_URI = "http://www.processmaker.com";

    @Value("${application.jwt.secret}")
    String SECRET;

    private final PesAllerService pesAllerService;
    private final PesRetourService pesRetourService;
    private final LocalAuthorityService localAuthorityService;
    private final SoapReturnGenerator soapReturnGenerator;
    private final ExternalRestService externalRestService;
    private ObjectFactory objectFactory;

    public PaullEndpoint(PesAllerService pesAllerService, LocalAuthorityService localAuthorityService,
            SoapReturnGenerator soapReturnGenerator, ExternalRestService externalRestService,
            PesRetourService pesRetourService) {
        this.pesAllerService = pesAllerService;
        this.localAuthorityService = localAuthorityService;
        this.soapReturnGenerator = soapReturnGenerator;
        this.externalRestService = externalRestService;
        this.pesRetourService = pesRetourService;
        this.objectFactory = new ObjectFactory();
    }

    PaullSoapToken getToken(String token) {
        if (token != null) {
            Claims tokenClaim = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();
            if (tokenClaim.getExpiration().before(new Date())) {
                return null;
            }
            String tokenParsed = tokenClaim.getSubject();

            ObjectMapper objectMapper = new ObjectMapper();
            PaullSoapToken node;
            try {
                node = objectMapper.readValue(tokenParsed, PaullSoapToken.class);
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
                return null;
            }

            return node;

        }
        return null;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "depotPESAllerRequest")
    public @ResponsePayload DepotPESAllerResponse depotPESAller(@RequestPayload DepotPESAllerRequest depotPesAller) {
        PaullSoapToken paullSoapToken = getToken(depotPesAller.getSessionId());
        if (paullSoapToken == null) {
            LOGGER.error("Session invalide ou expiré");
        }

        try {
            GenericAccount genericAccount = externalRestService.getGenericAccount(paullSoapToken.getAccountUuid());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        DepotPESAllerResponse depotPESAllerResponse = new DepotPESAllerResponse();

        return depotPESAllerResponse;
    }

}